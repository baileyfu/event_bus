package io.github.eventbus.core.sources.impl.database;

import io.github.eventbus.core.EBSub;
import io.github.eventbus.core.sources.Event;
import io.github.eventbus.core.sources.impl.database.dao.QueuedEventDAO;
import io.github.eventbus.core.sources.impl.database.model.QueuedEvent;
import io.github.eventbus.exception.EventbusException;
import io.github.eventbus.util.BeanConverter;
import org.apache.http.util.Asserts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 队列型(Queue)-事件只能被所有订阅的Terminal中的一个Terminal的一个节点消费一次<br/>
 * 确保事件被正常消费,消费失败可重复
 * @author ALi
 * @version 1.0
 * @date 2022-09-01 15:56
 * @description
 */
public class DatabaseQueueEventSource extends AbstractDatabaseEventSource implements EBSub.ListenedEventChangingListener {
    private String listenedEvents;
    protected QueuedEventDAO queuedEventDAO;
    public DatabaseQueueEventSource(String name, QueuedEventDAO queuedEventDAO) {
        super(name);
        Asserts.notNull(queuedEventDAO, "queuedEventDAO");
        this.queuedEventDAO = queuedEventDAO;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.eventSerializer = new Event.EventSerializer<QueuedEvent>() {
            @Override
            public QueuedEvent serialize(Event event) throws EventbusException {
                QueuedEvent queuedEvent = BeanConverter.eventToQueuedEvent(event);
                queuedEvent.setMessage(serializeMessage(event.getMessage()));
                queuedEvent.setSourceTerminal(serializeTerminal(event.getSourceTerminal()));
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
        queuedEventDAO.insert((QueuedEvent) eventSerializer.serialize(event));
    }

    @Override
    protected Map<Long, Event> fetchAndSetUnconsumed() throws Exception {
        if (listenedEvents == null) {
            logger.info("DatabaseQueueEventSource.fetchAndSetUnconsumed() listenedEvents is empty , no event will be fetched.");
            return null;
        }
        Map<Long, Event> unconsumedMap = null;
        List<QueuedEvent> unconsumedList = queuedEventDAO.selectUnconsumedThenUpdateConsumed(listenedEvents, consumeLimit);
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
        queuedEventDAO.updateStateToUnconsumed(eventId);
    }

    @Override
    protected void clean() throws Exception {
        if (listenedEvents != null) {
            queuedEventDAO.cleanConsumed(listenedEvents, cleanCycle);
        }
    }

    @Override
    public void notifyCausedByListenedEventChanging(List<String> listenedEvents) {
        if (listenedEvents != null && listenedEvents.size() > 0) {
            StringBuilder temp=new StringBuilder();
            for(String listenedEvent : listenedEvents) {
                temp.append(listenedEvent).append(",");
            }
            this.listenedEvents = temp.substring(0, temp.length() - 1);
        } else {
            this.listenedEvents = null;
        }
    }
}
