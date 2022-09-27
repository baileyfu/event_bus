package io.github.eventbus.core.sources.impl.database.dao.mybatis;

import io.github.eventbus.core.sources.impl.database.dao.QueuedEventDAO;
import io.github.eventbus.core.sources.impl.database.model.QueuedEvent;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.StatementType;

import java.util.List;

/**
 * Eventbus提供的SQL实现
 * @author ALi
 * @version 1.0
 * @date 2022-09-07 17:07
 * @description
 */
public interface QueuedEventWithDumpAnnotationMapper extends QueuedEventDAO {
    @Insert("insert into eventbus_queued_event(serial_id,name,message,message_type,source_terminal,state,create_time) values(#{serialId},#{name},#{message},#{messageType},#{sourceTerminal}," + QueuedEvent.STATE_UNCONSUMED + ",now())")
    @Options(useCache = false, useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Override
    int insert(QueuedEvent queuedEvent);

    @Select("{CALL eventbusSelectUnconsumedThenUpdateConsumedForQueued(#{eventNames,mode=IN,jdbcType=VARCHAR},#{limit,mode=IN,jdbcType=BIT},#{targetTerminal,mode=IN,jdbcType=VARCHAR})}")
    @Options(useCache = false, statementType = StatementType.CALLABLE)
    @Override
    List<QueuedEvent> selectUnconsumedThenUpdateConsumed(@Param("eventNames") String eventNames,@Param("limit") int limit, @Param("targetTerminal") String targetTerminal);

    @Update("update eventbus_queued_event set state = " + QueuedEvent.STATE_UNCONSUMED + " where id = #{id}")
    @Options(useCache = false)
    @Override
    int updateStateToUnconsumed(@Param("id") long id);

    /**
     * 转储被消费时间超过x小时的事件<br/>
     * 转储表默认为eventbus_queued_event_dumped
     * @return
     */
    @Select("{CALL eventbusDumpConsumedForQueued(#{eventNames,mode=IN,jdbcType=VARCHAR},#{cycleHours,mode=IN,jdbcType=BIT})}")
    @Options(useCache = false, statementType = StatementType.CALLABLE)
    @Override
    int cleanConsumed(@Param("eventNames") String eventNames, @Param("cycleHours") int cycleHours);
}
