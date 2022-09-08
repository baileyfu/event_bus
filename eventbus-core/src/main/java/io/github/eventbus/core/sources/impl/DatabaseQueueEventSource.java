package io.github.eventbus.core.sources.impl;

import io.github.eventbus.core.sources.Event;
import io.github.eventbus.core.sources.impl.database.mybatis.dao.QueuedEventMapper;
import io.github.eventbus.core.sources.impl.database.mybatis.model.QueuedEvent;
import io.github.eventbus.exception.EventbusException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Asserts;

import java.util.*;

/**
 * 队列型(Queue)<br/>
 * 确保事件被正常消费,消费失败可重复
 * @author ALi
 * @version 1.0
 * @date 2022-09-01 15:56
 * @description
 */
public class DatabaseQueueEventSource extends AbstractDatabaseEventSource {
    protected QueuedEventMapper queuedEventMapper;
    public DatabaseQueueEventSource(String name, QueuedEventMapper queuedEventMapper) {
        super(name);
        Asserts.notNull(queuedEventMapper, "QueuedEventMapper");
        this.queuedEventMapper = queuedEventMapper;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.eventSerializer = new Event.EventSerializer<QueuedEvent>() {
            @Override
            public QueuedEvent serialize(Event event) throws EventbusException {
                QueuedEvent queuedEvent = new QueuedEvent();
                queuedEvent.setSerialId(event.getSerialId());
                queuedEvent.setName(event.getName());
                queuedEvent.setState(QueuedEvent.STATE_UNCONSUMED);
                queuedEvent.setMessage(serializeMessage(event.getMessage()));
                Class messageType = event.getMessageType();
                queuedEvent.setMessageType(messageType == null ? StringUtils.EMPTY : messageType.getName());
                queuedEvent.setSourceTerminal(serializeTerminal(event.getSourceTerminal()));
                queuedEvent.setCreateTime(new Date());
                return queuedEvent;
            }

            @Override
            public Event deserialize(QueuedEvent queuedEvent) throws EventbusException {
                return Event.EventBuilder.newInstance()
                        .name(queuedEvent.getName())
                        .message(deserializeMessage(queuedEvent.getMessage(), queuedEvent.getMessageType()))
                        .sourceTerminal(deserializeTerminal(queuedEvent.getSourceTerminal()))
                        .build(queuedEvent.getSerialId());
            }
        };
    }

    @Override
    protected void save(Event event) throws Exception {
        queuedEventMapper.insert((QueuedEvent) eventSerializer.serialize(event));
    }

    @Override
    protected Map<Long, Event> fetchAndSetUnconsumed() throws Exception {
        Map<Long, Event> unconsumedMap = null;
        List<QueuedEvent> unconsumedList = queuedEventMapper.selectUnconsumedThenUpdateConsumed(QueuedEvent.STATE_UNCONSUMED, limit);
        if (unconsumedList != null && unconsumedList.size() > 0) {
            List<Long> queuedEventIdList = new ArrayList<>();
            unconsumedMap = unconsumedList.parallelStream().reduce(new HashMap<>(), (map, queuedEvent) -> {
                try {
                    map.put(queuedEvent.getId(), eventSerializer.deserialize(queuedEvent));
                } catch (EventbusException ee) {
                    throw new RuntimeException("deserialize QueuedEvent '" + queuedEvent + "' error !", ee);
                }
                queuedEventIdList.add(queuedEvent.getId());
                return map;
            }, (m, n) -> m);
        }
        return unconsumedMap;
    }

    @Override
    protected void setUnconsumed(long eventId) throws Exception {
        queuedEventMapper.updateStateToUnconsumed(eventId);
    }
}
