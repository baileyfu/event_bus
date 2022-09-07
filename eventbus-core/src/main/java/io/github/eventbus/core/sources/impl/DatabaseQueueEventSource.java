package io.github.eventbus.core.sources.impl;

import io.github.eventbus.core.sources.Event;
import io.github.eventbus.core.sources.impl.database.mybatis.dao.QueuedEventMapper;
import io.github.eventbus.core.sources.impl.database.mybatis.model.QueuedEvent;
import org.apache.http.util.Asserts;

import java.util.Map;

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
    protected void save(Event event) throws Exception {
        QueuedEvent queuedEvent = null;
        queuedEventMapper.insert(queuedEvent);
    }

    @Override
    protected Map<Long, Event> fetchAndSetUnconsumed() {
        //TODO
        return null;
    }

    @Override
    protected void setUnconsumed(long eventId) throws Exception {
        //TODO
    }
}
