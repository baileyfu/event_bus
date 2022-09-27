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
public interface QueuedEventAnnotationMapper extends QueuedEventDAO {
    @Insert("insert into eventbus_queued_event(serial_id,name,message,message_type,source_terminal,state,create_time) values(#{serialId},#{name},#{message},#{messageType},#{sourceTerminal}," + QueuedEvent.STATE_UNCONSUMED + ",now())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
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
     * 直接删除被消费时间超过x小时的事件
     * @return
     */
    @Delete("delete from eventbus_queued_event where state = " + QueuedEvent.STATE_CONSUMED + " and name in #{eventNames} and DATE_ADD(create_time,INTERVAL ${cycleHours} HOUR) < now()")
    @Options(useCache = false)
    @Override
    int cleanConsumed(@Param("eventNames") String eventNames, @Param("cycleHours") int cycleHours);
}
