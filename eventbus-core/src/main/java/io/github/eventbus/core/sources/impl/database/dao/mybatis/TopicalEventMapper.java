package io.github.eventbus.core.sources.impl.database.dao.mybatis;

import io.github.eventbus.core.sources.impl.database.mybatis.model.TopicalEvent;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-07 16:44
 * @description
 */
public interface TopicalEventMapper{
    int insert(TopicalEvent topicalEvent);
}
