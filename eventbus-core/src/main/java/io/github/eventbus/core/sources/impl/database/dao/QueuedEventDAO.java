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
    /**
     * 插入事件
     * @param queuedEvent
     * @return
     */
    int insert(QueuedEvent queuedEvent);

    /**
     * 查询未消费事件并设置未已消费
     * @param eventNameList 指定要消费的事件名
     * @param limit
     * @return
     */
    List<QueuedEvent> selectUnconsumedThenUpdateConsumed(List<String> eventNameList, int limit);

    /**
     * 将指定事件ID设置为未消费状态
     * @param id
     * @return
     */
    int updateStateToUnconsumed(long id);

    /**
     * 清除已消费事件<br/>
     * 可自定义直接清除或转储
     * @param cycleHours
     * @return
     */
    int cleanConsumed(int cycleHours);
}
