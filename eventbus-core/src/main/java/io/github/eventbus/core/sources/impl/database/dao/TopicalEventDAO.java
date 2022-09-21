package io.github.eventbus.core.sources.impl.database.dao;

import io.github.eventbus.core.sources.impl.database.model.TopicalEvent;

import java.util.List;

/**
 * 发布-订阅型(Topic)数据源DAO
 * @author ALi
 * @version 1.0
 * @date 2022-09-08 21:26
 * @description
 */
public interface TopicalEventDAO {
    int insert(TopicalEvent topicalEvent);
    List<TopicalEvent> selectUnconsumedThenUpdateConsumed(String terminalId, int limit);
    int updateStateToUnconsumed(long id);
    int cleanConsumed(String terminalId, int cycleHours);
}
