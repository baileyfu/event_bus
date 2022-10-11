package io.github.eventbus.core.sources.impl.database;

import io.github.ali.commons.beanutils.BeanCopierUtils;
import io.github.ali.commons.variable.MixedActionGenerator;
import io.github.eventbus.constants.EventSourceConfigConst;
import io.github.eventbus.core.event.Event;
import io.github.eventbus.core.event.EventSerializer;
import io.github.eventbus.core.monitor.ResourceMonitor;
import io.github.eventbus.core.sources.impl.database.dao.TopicalEventDAO;
import io.github.eventbus.core.sources.impl.database.dao.TopicalEventTerminalDAO;
import io.github.eventbus.core.sources.impl.database.model.TopicalEvent;
import io.github.eventbus.core.sources.impl.database.model.TopicalEventTerminal;
import io.github.eventbus.core.terminal.Terminal;
import io.github.eventbus.core.terminal.TerminalFactory;
import io.github.eventbus.exception.EventbusException;
import io.github.eventbus.util.BeanConverter;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.util.Asserts;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 发布-订阅型(Topic)-事件发给所有订阅的Terminal，但只能被每个Terminal集群节点中的一个节点消费一次<br/>
 * 确保事件被正常消费,消费失败可重复<br/>
 * 负责维护节点（激活/失活）
 * @author ALi
 * @version 1.0
 * @date 2022-09-07 11:16
 * @description
 */
public class DatabaseTopicEventSource extends AbstractDatabaseEventSource {
    public static final int INACTIVATE_ACTION_INTERVAL_HOURS = 1;

    public static final boolean DEFAULT_INACTIVATE_REQUIRED = false;

    public static final int DEFAULT_INACTIVATE_CYCLE = 24;
    public static final int MIN_INACTIVATE_CYCLE = 2;

    private TopicalEventDAO topicalEventDAO;
    private TopicalEventTerminalDAO topicalEventTerminalDAO;
    //注册到TopicalEventTerminal的主键
    private String terminalIdForRegister;
    //是否将超过24小时未活跃的客户端设置未失活(将不再接收任何事件)
    private Boolean inactivateRequired;
    //失活周期,单位：小时;最小为2小时
    private int inactivateCycle;

    public DatabaseTopicEventSource(String name, TopicalEventDAO topicalEventDAO, TopicalEventTerminalDAO topicalEventTerminalDAO) {
        super(name);
        Asserts.notNull(topicalEventDAO, "TopicalEventDAO");
        this.topicalEventDAO = topicalEventDAO;
        Asserts.notNull(topicalEventDAO, "TopicalEventTerminalDAO");
        this.topicalEventTerminalDAO = topicalEventTerminalDAO;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        if (inactivateRequired == null) {
            setInactivateRequired(Boolean.valueOf(environment.getProperty(EventSourceConfigConst.MANUAL_DATABASE_TOPIC_INACTIVATE_REQUIRED, String.valueOf(DEFAULT_INACTIVATE_REQUIRED))));
        }
        if (inactivateCycle < MIN_INACTIVATE_CYCLE) {
            setInactivateCycle(Integer.valueOf(environment.getProperty(EventSourceConfigConst.MANUAL_DATABASE_TOPIC_INACTIVATE_CYCLE, String.valueOf(DEFAULT_INACTIVATE_CYCLE))));
        }
        this.setEventSerializer(null);
        //启动定时(1小时)激活当前节点并剔除失活节点
        final String actionName = this + ".inactivateTerminal";
        ResourceMonitor.registerResource(new ResourceMonitor.Switch() {
            @Override
            public String identify() {
                return actionName;
            }
            @Override
            public void doOn() throws Exception {
                terminalIdForRegister = createCurrentTerminalId(TerminalFactory.create());
                registerTerminal();
                MixedActionGenerator.loadAction(actionName, INACTIVATE_ACTION_INTERVAL_HOURS, TimeUnit.HOURS, () -> {
                    try {
                        activateTerminal();
                        if (inactivateRequired) {
                            inactivateTerminal();
                        }
                    } catch (Exception e) {
                        logger.error("the action of " + actionName + " error!", e);
                    }
                });
            }
            @Override
            public void doOff() throws Exception {
                MixedActionGenerator.unloadAction(actionName, false);
            }
        });
    }

    protected String createCurrentTerminalId(Terminal terminal){
        return terminal.getName();
    }
    /**
     * DatabaseTopicEventSource禁止自定义序列化
     * @param eventSerializer 将被忽略
     */
    @Override
    public void setEventSerializer(EventSerializer eventSerializer) {
        super.setEventSerializer(TOPIC_EVENT_SERIALIZER);
    }
    /**
     * 注册当前终端节点
     */
    private void registerTerminal(){
        String eventSourceName = this.getName();
        TopicalEventTerminal topicalEventTerminal = topicalEventTerminalDAO.selectByEventSourceNameAndTerminalId(eventSourceName, terminalIdForRegister);
        if (topicalEventTerminal == null) {
            topicalEventTerminal = new TopicalEventTerminal();
            topicalEventTerminal.setEventSourceName(eventSourceName);
            topicalEventTerminal.setTerminalId(terminalIdForRegister);
            topicalEventTerminal.setState(TopicalEventTerminal.TERMINAL_STATE_NORMAL);
            topicalEventTerminal.setLastActiveTime(new Date());
            topicalEventTerminalDAO.insert(topicalEventTerminal);
        } else {
            //若当前节点不再活跃则激活
            activateTerminal();
        }
    }

    /**
     * 激活当前节点并更新最后活跃时间
     */
    private void activateTerminal(){
        topicalEventTerminalDAO.updateLastActiveTime(getName(), terminalIdForRegister);
    }
    /**
     * 剔除不再活跃的节点,使其不再收到事件
     */
    private void inactivateTerminal() {
        String eventSourceName = this.getName();
        List<TopicalEventTerminal> activeTerminals = topicalEventTerminalDAO.selectActive(eventSourceName);
        if (activeTerminals != null && activeTerminals.size() > 0) {
            for (TopicalEventTerminal topicalEventTerminal : activeTerminals) {
                //失活条件:距离最后一次激活超过x小时
                Date lastActiveTime = topicalEventTerminal.getLastActiveTime();
                if (lastActiveTime == null || DateUtils.addHours(lastActiveTime, inactivateCycle).before(new Date())) {
                    topicalEventTerminalDAO.updateStateToDowntime(eventSourceName, topicalEventTerminal.getTerminalId());
                }
            }
        }
    }

    public void setInactivateRequired(boolean inactivateRequired) {
        this.inactivateRequired = inactivateRequired;
    }

    public void setInactivateCycle(int inactivateCycle) {
        this.inactivateCycle = inactivateCycle;
        if (this.inactivateCycle < MIN_INACTIVATE_CYCLE) {
            this.inactivateCycle = DEFAULT_INACTIVATE_CYCLE;
            logger.warn(EventSourceConfigConst.MANUAL_DATABASE_TOPIC_INACTIVATE_CYCLE + " value is " + inactivateCycle + " , reset to " + DEFAULT_INACTIVATE_CYCLE);
        }
    }

    @Override
    protected void save(String eventName, Object serializedEvent) throws Exception {
        //直接使用传入的对象
        TopicalEvent topicalEvent = (TopicalEvent) serializedEvent;
        //TODO 缓存
        List<TopicalEventTerminal> activeTerminal = topicalEventTerminalDAO.selectActive(this.getName());
        if (activeTerminal != null && activeTerminal.size() > 0) {
            for (TopicalEventTerminal terminal : activeTerminal) {
                topicalEvent.setTerminalId(terminal.getTerminalId());
                topicalEventDAO.insert(topicalEvent);
            }
        }
    }

    @Override
    protected List<SerializedEventWrapper> fetchAndSetConsumed() {
        List<TopicalEvent> unconsumedList = topicalEventDAO.selectUnconsumedThenUpdateConsumed(terminalIdForRegister, consumeLimit, serializedTerminalForConsumed);
        return unconsumedList != null && unconsumedList.size() > 0
                                                            ? unconsumedList.parallelStream()
                                                                            .map(topicalEvent -> new SerializedEventWrapper(topicalEvent.getId(), topicalEvent))
                                                                            .collect(Collectors.toList())
                                                            : null;
    }

    @Override
    protected void setUnconsumed(SerializedEventWrapper serializedEventWrapper) throws Exception {
        topicalEventDAO.updateStateToUnconsumed(serializedEventWrapper.getKey());
    }

    @Override
    protected void clean() throws Exception {
        topicalEventDAO.cleanConsumed(terminalIdForRegister, cleanCycle);
    }

    private static final EventSerializer TOPIC_EVENT_SERIALIZER = new EventSerializer<TopicalEvent>() {
        @Override
        public TopicalEvent serialize(Event event) throws EventbusException {
            TopicalEvent topicalEvent = BeanConverter.eventToTopicalEvent(event);
            topicalEvent.setMessage(serializeMessage(event.getMessage()));
            topicalEvent.setSourceTerminal(serializeTerminal(event.getSourceTerminal()));
            return topicalEvent;
        }

        @Override
        public Event deserialize(TopicalEvent topicalEvent) throws EventbusException {
            return Event.EventBuilder.newInstance()
                    .name(topicalEvent.getName())
                    .message(deserializeMessage(topicalEvent.getMessage(), topicalEvent.getMessageType()))
                    .sourceTerminal(deserializeTerminal(topicalEvent.getSourceTerminal()))
                    .build(topicalEvent.getSerialId());
        }
    };
}
