package io.github.eventbus.core.sources.impl;

import io.github.ali.commons.beanutils.BeanCopierUtils;
import io.github.ali.commons.variable.MixedActionGenerator;
import io.github.eventbus.core.sources.Event;
import io.github.eventbus.core.sources.impl.database.dao.TopicalEventDAO;
import io.github.eventbus.core.sources.impl.database.dao.TopicalEventTerminalDAO;
import io.github.eventbus.core.sources.impl.database.model.TopicalEvent;
import io.github.eventbus.core.sources.impl.database.model.TopicalEventTerminal;
import io.github.eventbus.core.terminal.Terminal;
import io.github.eventbus.core.terminal.TerminalFactory;
import io.github.eventbus.exception.EventbusException;
import io.github.eventbus.util.BeanConverter;
import org.apache.http.util.Asserts;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 发布-订阅型(Topic)-事件发给所有订阅的Terminal，但只能被Terminal集群节点中的一个节点消费一次<br/>
 * 确保事件被正常消费,消费失败可重复
 *
 * @author ALi
 * @version 1.0
 * @date 2022-09-07 11:16
 * @description
 */
public class DatabaseTopicEventSource extends AbstractDatabaseEventSource {
    private TopicalEventDAO topicalEventDAO;
    private TopicalEventTerminalDAO topicalEventTerminalDAO;
    private String currentTerminalId;

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
        //启动定时(1天)剔除失活节点
        String actionName = this + ".inactivateTerminal";
        MixedActionGenerator.loadAction(actionName, 1, TimeUnit.DAYS, () -> {
            try {
                inactivateTerminal();
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
        TopicalEventTerminal topicalEventTerminal = topicalEventTerminalDAO.selectByTerminalId(currentTerminalId);
        if (topicalEventTerminal == null) {
            topicalEventTerminal = new TopicalEventTerminal();
            topicalEventTerminal.setTerminalId(currentTerminalId);
            topicalEventTerminal.setState(TopicalEventTerminal.TERMINAL_STATE_NORMAL);
            topicalEventTerminal.setLastActiveTime(new Date());
            topicalEventTerminalDAO.insert(topicalEventTerminal);
        } else {
            topicalEventTerminalDAO.updateLastActiveTime(currentTerminalId);
        }
    }

    /**
     * 剔除不再活跃的节点,使其不再收到事件
     */
    private void inactivateTerminal() {
        //TODO
    }

    @Override
    protected void save(Event event) throws Exception {
        TopicalEvent topicalEvent = (TopicalEvent) eventSerializer.serialize(event);
        //TODO 缓存
        List<TopicalEventTerminal> activeTerminal = topicalEventTerminalDAO.selectActive();
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
        List<TopicalEvent> unconsumedList = topicalEventDAO.selectUnconsumedThenUpdateConsumed(currentTerminalId, limit);
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
