package io.github.eventbus.core.sources.impl;

import io.github.eventbus.core.sources.Event;
import io.github.eventbus.core.sources.impl.database.dao.TopicalEventDAO;
import org.apache.http.util.Asserts;

import java.util.Map;

/**
 * 发布-订阅型(Topic)<br/>
 * 确保事件被正常消费,消费失败可重复
 *
 * @author ALi
 * @version 1.0
 * @date 2022-09-07 11:16
 * @description
 */
public class DatabaseTopicEventSource extends AbstractDatabaseEventSource {
    private TopicalEventDAO topicalEventDAO;

    public DatabaseTopicEventSource(String name, TopicalEventDAO topicalEventDAO) {
        super(name);
        Asserts.notNull(topicalEventDAO, "topicalEventDAO");
        this.topicalEventDAO = topicalEventDAO;
    }

    @Override
    protected void save(Event event) throws Exception {
    }

    @Override
    protected Map<Long, Event> fetchAndSetUnconsumed() {
        return null;
    }

    @Override
    protected void setUnconsumed(long eventId) throws Exception {

    }
}
