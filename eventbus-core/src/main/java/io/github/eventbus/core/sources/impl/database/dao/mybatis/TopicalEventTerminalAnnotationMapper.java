package io.github.eventbus.core.sources.impl.database.dao.mybatis;

import io.github.eventbus.core.sources.impl.database.dao.TopicalEventTerminalDAO;
import io.github.eventbus.core.sources.impl.database.model.TopicalEvent;
import io.github.eventbus.core.sources.impl.database.model.TopicalEventTerminal;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-15 10:59
 * @description
 */
public interface TopicalEventTerminalAnnotationMapper extends TopicalEventTerminalDAO {
    @Insert("insert into eventbus_topical_event_terminal(terminal_id,state,create_time) values(#{terminalId}," + TopicalEventTerminal.TERMINAL_STATE_NORMAL + ",now())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Override
    int insert(TopicalEventTerminal topicalEventTerminal);

    @Update("update eventbus_topical_event_terminal set state=" + TopicalEventTerminal.TERMINAL_STATE_NORMAL + " , last_active_time = now() where terminal_id = #{terminalId}")
    @Options(useCache = false)
    @Override
    int updateLastActiveTime(String terminalId);

    @Update("update eventbus_topical_event_terminal set state=" + TopicalEventTerminal.TERMINAL_STATE_DOWNTIME + " where terminal_id = #{terminalId}")
    @Options(useCache = false)
    @Override
    int updateStateToDowntime(String terminalId);

    @Select("select * from eventbus_topical_event_terminal where terminal_id = #{terminalId}")
    @Options(useCache = false)
    @Override
    TopicalEventTerminal selectByTerminalId(String terminalId);

    @Select("select * from eventbus_topical_event_terminal where state = " + TopicalEventTerminal.TERMINAL_STATE_NORMAL)
    @Options(useCache = false)
    @Override
    List<TopicalEventTerminal> selectActive();
}
