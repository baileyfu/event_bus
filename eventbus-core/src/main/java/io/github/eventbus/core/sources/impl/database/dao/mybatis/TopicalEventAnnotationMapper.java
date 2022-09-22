package io.github.eventbus.core.sources.impl.database.dao.mybatis;


import io.github.eventbus.core.sources.impl.database.dao.TopicalEventDAO;
import io.github.eventbus.core.sources.impl.database.model.TopicalEvent;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.StatementType;

import java.util.List;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-07 16:44
 * @description
 */
public interface TopicalEventAnnotationMapper extends TopicalEventDAO {
    @Insert("insert into eventbus_topical_event(terminal_id,serial_id,name,message,message_type,source_terminal,state,create_time) values(#{terminalId},#{serialId},#{name},#{message},#{messageType},#{sourceTerminal}," + TopicalEvent.STATE_UNCONSUMED + ",now())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Override
    int insert(TopicalEvent topicalEvent);

    @Select("{CALL eventbusSelectUnconsumedThenUpdateConsumedForTopical(#{terminalId,mode=IN,jdbcType=VARCHAR},#{limit,mode=IN,jdbcType=BIT})}")
    @Options(useCache = false, statementType = StatementType.CALLABLE)
    @Override
    List<TopicalEvent> selectUnconsumedThenUpdateConsumed(@Param("terminalId") String terminalId,@Param("limit") int limit);

    @Update("update eventbus_topical_event set state = " + TopicalEvent.STATE_UNCONSUMED + " where id = #{id}")
    @Options(useCache = false)
    @Override
    int updateStateToUnconsumed(@Param("id") long id);

    /**
     * 直接删除被消费时间超过x小时的事件
     * @return
     */
    @Delete("delete from eventbus_topical_event where state = " + TopicalEvent.STATE_CONSUMED + " and terminal_id = #{terminalId} and DATE_ADD(create_time,INTERVAL ${cycleHours} HOUR) < now()")
    @Options(useCache = false)
    @Override
    int cleanConsumed(@Param("terminalId") String terminalId, @Param("cycleHours") int cycleHours);
}
