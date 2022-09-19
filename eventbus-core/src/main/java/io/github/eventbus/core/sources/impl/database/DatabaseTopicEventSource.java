package io.github.eventbus.core.sources.impl.database;

import io.github.ali.commons.beanutils.BeanCopierUtils;
import io.github.ali.commons.variable.MixedActionGenerator;
import io.github.eventbus.constants.EventSourceConfigConst;
import io.github.eventbus.core.sources.Event;
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

/**
 * 发布-订阅型(Topic)-事件发给所有订阅的Terminal，但只能被每个Terminal集群节点中的一个节点消费一次<br/>
 * 确保事件被正常消费,消费失败可重复
 *
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
    private String currentTerminalId;
    //是否将超过24小时未活跃的客户端设置未失活(将不再接收任何事件)
    private Boolean inactivateRequired;
    //失活周期,单位：小时;最小为2小时
    private int inactivateCycle;

    public DatabaseTopicEventSource(String name, TopicalEventDAO topicalEventDAO, TopicalEventTerminalDAO topicalEventTerminalDAO) {
        super(name);
        Asserts.notNull(topicalEventDAO, "topicalEventDAO");
        this.topicalEventDAO = topicalEventDAO;
        Asserts.notNull(topicalEventDAO, "topicalEventTerminalDAO");
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
        this.eventSerializer = new Event.EventSerializer<TopicalEvent>() {
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
        currentTerminalId = createCurrentTerminalId(TerminalFactory.create());
        registerTerminal();
        //启动定时(1小时)激活当前节点并剔除失活节点
        String actionName = this + ".inactivateTerminal";
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
    protected String createCurrentTerminalId(Terminal terminal){
        return terminal.getName();
    }
    /**
     * 注册当前终端节点
     */
    private void registerTerminal(){
        String eventSourceName = this.getName();
        TopicalEventTerminal topicalEventTerminal = topicalEventTerminalDAO.selectByEventSourceNameAndTerminalId(eventSourceName, currentTerminalId);
        if (topicalEventTerminal == null) {
            topicalEventTerminal = new TopicalEventTerminal();
            topicalEventTerminal.setEventSourceName(eventSourceName);
            topicalEventTerminal.setTerminalId(currentTerminalId);
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
        topicalEventTerminalDAO.updateLastActiveTime(getName(), currentTerminalId);
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
    protected void save(Event event) throws Exception {
        TopicalEvent topicalEvent = (TopicalEvent) eventSerializer.serialize(event);
        //TODO 缓存
        List<TopicalEventTerminal> activeTerminal = topicalEventTerminalDAO.selectActive(this.getName());
        if (activeTerminal != null && activeTerminal.size() > 0) {
            for (TopicalEventTerminal terminal : activeTerminal) {
                TopicalEvent target = BeanCopierUtils.copyOne2One(topicalEvent, TopicalEvent.class);
                target.setTerminalId(terminal.getTerminalId());
                topicalEventDAO.insert(target);
            }
        }
    }

    @Override
    protected Map<Long, Event> fetchAndSetUnconsumed() {
        Map<Long, Event> unconsumedMap = null;
        List<TopicalEvent> unconsumedList = topicalEventDAO.selectUnconsumedThenUpdateConsumed(currentTerminalId, consumeLimit);
        if (unconsumedList != null && unconsumedList.size() > 0) {
            List<Long> queuedEventIdList = new ArrayList<>();
            unconsumedMap = unconsumedList.parallelStream().reduce(new HashMap<>(), (map, topicalEvent) -> {
                try {
                    map.put(topicalEvent.getId(), eventSerializer.deserialize(topicalEvent));
                } catch (EventbusException ee) {
                    throw new RuntimeException("deserialize TopicalEvent '" + topicalEvent + "' error !", ee);
                }
                queuedEventIdList.add(topicalEvent.getId());
                return map;
            }, (m, n) -> m);
        }
        return unconsumedMap;
    }

    @Override
    protected void setUnconsumed(long eventId) throws Exception {
        topicalEventDAO.updateStateToUnconsumed(eventId);
    }

    @Override
    protected void clean() throws Exception {
        topicalEventDAO.cleanConsumed(currentTerminalId, cleanCycle);
    }
}
