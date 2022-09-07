package io.github.eventbus.core.sources.impl.database.mybatis.dao;

import io.github.eventbus.core.sources.impl.database.mybatis.model.QueuedEvent;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-07 17:07
 * @description
 */
public interface QueuedEventMapper {
    @Insert("insert into eventbus_queued_event(name,message,sourceTerminal,state,createTime) values(#{name},#{message},#{sourceTerminal},#{state},now())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(QueuedEvent queuedEvent);
}
