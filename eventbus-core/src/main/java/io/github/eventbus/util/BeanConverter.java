package io.github.eventbus.util;

import io.github.eventbus.core.event.Event;
import io.github.eventbus.core.sources.impl.database.model.QueuedEvent;
import io.github.eventbus.core.sources.impl.database.model.TopicalEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-14 16:47
 * @description
 */
public class BeanConverter {
    public static QueuedEvent eventToQueuedEvent(Event event) {
        QueuedEvent queuedEvent = new QueuedEvent();
        fillQueuedEvent(queuedEvent, event);
        return queuedEvent;
    }

    public static TopicalEvent eventToTopicalEvent(Event event) {
        TopicalEvent topicalEvent = new TopicalEvent();
        fillQueuedEvent(topicalEvent, event);
        return topicalEvent;
    }
    private static void fillQueuedEvent(QueuedEvent queuedEvent,Event event){
        queuedEvent.setSerialId(event.getSerialId());
        queuedEvent.setName(event.getName());
        queuedEvent.setState(QueuedEvent.STATE_UNCONSUMED);
        Class messageType = event.getMessageType();
        queuedEvent.setMessageType(messageType == null ? StringUtils.EMPTY : messageType.getName());
        queuedEvent.setCreateTime(new Date());
    }
}
