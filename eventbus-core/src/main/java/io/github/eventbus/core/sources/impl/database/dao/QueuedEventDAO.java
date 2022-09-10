package io.github.eventbus.core.sources.impl.database.dao;

import io.github.eventbus.core.sources.impl.database.model.QueuedEvent;

import java.util.List;

/**
 * 队列型(Queue)数据源DAO
 * @author ALi
 * @version 1.0
 * @date 2022-09-08 21:24
 * @description
 */
public interface QueuedEventDAO {
    int insert(QueuedEvent queuedEvent);

    List<QueuedEvent> selectUnconsumedThenUpdateConsumed(int state, int limit);

    int updateStateToUnconsumed(long id);
}
