package io.github.eventbus.core.sources.impl.database;

import io.github.eventbus.core.EBSub;
import io.github.eventbus.core.event.Event;
import io.github.eventbus.core.event.EventSerializer;
import io.github.eventbus.core.sources.impl.database.dao.QueuedEventDAO;
import io.github.eventbus.core.sources.impl.database.model.QueuedEvent;
import io.github.eventbus.exception.EventbusException;
import io.github.eventbus.util.BeanConverter;
import org.apache.http.util.Asserts;

import java.util.List;
import java.util.stream.Collectors;

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
        Asserts.notNull(queuedEventDAO, "QueuedEventDAO");
        this.queuedEventDAO = queuedEventDAO;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.setEventSerializer(null);
    }

    /**
     * DatabaseQueueEventSource禁止自定义序列化
     * @param eventSerializer 将被忽略
     */
    @Override
    public void setEventSerializer(EventSerializer eventSerializer) {
        super.setEventSerializer(QUEUE_EVENT_SERIALIZER);
    }

    @Override
    protected void save(String eventName, Object serializedEvent) throws Exception {
        queuedEventDAO.insert((QueuedEvent) serializedEvent);
    }

    @Override
    protected List<SerializedEventWrapper> fetchAndSetConsumed() throws Exception {
        if (listenedEvents == null) {
            logger.info("DatabaseQueueEventSource.fetchAndSetUnconsumed() listenedEvents is empty , no event will be fetched.");
            return null;
        }
        List<QueuedEvent> unconsumedList = queuedEventDAO.selectUnconsumedThenUpdateConsumed(listenedEvents, consumeLimit, getTargetTerminal());
        return unconsumedList != null && unconsumedList.size() > 0
                                                            ? unconsumedList.parallelStream()
                                                                            .map(queuedEvent -> new SerializedEventWrapper(queuedEvent.getId(), queuedEvent))
                                                                            .collect(Collectors.toList())
                                                            : null;
    }

    @Override
    protected void setUnconsumed(SerializedEventWrapper serializedEventWrapper) throws Exception {
        queuedEventDAO.updateStateToUnconsumed(serializedEventWrapper.getKey());
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
    private static final EventSerializer QUEUE_EVENT_SERIALIZER = new EventSerializer<QueuedEvent>() {
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
