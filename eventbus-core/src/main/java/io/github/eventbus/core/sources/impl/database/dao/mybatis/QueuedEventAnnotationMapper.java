package io.github.eventbus.core.sources.impl.database.dao.mybatis;

import io.github.eventbus.core.sources.impl.database.dao.QueuedEventDAO;
import io.github.eventbus.core.sources.impl.database.model.QueuedEvent;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * Eventbus提供的SQL实现
 * @author ALi
 * @version 1.0
 * @date 2022-09-07 17:07
 * @description
 */
public interface QueuedEventAnnotationMapper extends QueuedEventDAO {
    @Insert("insert into eventbus_queued_event(serial_id,name,message,message_type,source_terminal,state,create_time) values(#{serialId},#{name},#{message},#{messageType},#{sourceTerminal},#{state},now())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(QueuedEvent queuedEvent);

    //TODO select and update
    @Select("begin;select * from eventbus_queued_event where state=#{state} limit #{limit} for update;commit;")
    @Options(useCache = false)
    List<QueuedEvent> selectUnconsumedThenUpdateConsumed(int state, int limit);

    @Update("update eventbus_queued_event set state=" + QueuedEvent.STATE_CONSUMED+" where id in" +
            "<foreach collection='eventIdList' index='index' item='item' open='(' separator=',' close=')'>#{item}</foreach>")
    @Options(useCache = false)
    //int updateStateToConsumed(@Param("eventIdList")List<Long> eventIdList);

    @Update("update eventbus_queued_event set state=" + QueuedEvent.STATE_UNCONSUMED+" where id in(#{id})")
    @Options(useCache = false)
    int updateStateToUnconsumed(long id);
}
