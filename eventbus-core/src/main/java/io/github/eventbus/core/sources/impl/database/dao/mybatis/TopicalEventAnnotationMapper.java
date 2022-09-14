package io.github.eventbus.core.sources.impl.database.dao.mybatis;


import io.github.eventbus.core.sources.impl.database.dao.TopicalEventDAO;
import io.github.eventbus.core.sources.impl.database.model.TopicalEvent;

import java.util.List;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-07 16:44
 * @description
 */
public interface TopicalEventAnnotationMapper extends TopicalEventDAO {
    int insert(TopicalEvent topicalEvent);

    List<TopicalEvent> selectUnconsumedThenUpdateConsumed(int limit);

    int updateStateToUnconsumed(long id);

    int cleanConsumed(int cycleHours);
}
