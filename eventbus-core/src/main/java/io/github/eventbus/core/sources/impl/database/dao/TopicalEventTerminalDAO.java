package io.github.eventbus.core.sources.impl.database.dao;

import io.github.eventbus.core.sources.impl.database.model.TopicalEventTerminal;

import java.util.List;

/**
 * 发布-订阅型(Topic)数据源-终端列表DAO
 * @author ALi
 * @version 1.0
 * @date 2022-09-14 17:00
 * @description
 */
public interface TopicalEventTerminalDAO {
    int insert(TopicalEventTerminal topicalEventTerminal);
    int updateLastActiveTime(String terminalId);
    TopicalEventTerminal selectByTerminalId(String terminalId);
    /**
     * 所有当前活跃的客户端(状态正常且处于活跃期)
     * @return
     */
    List<TopicalEventTerminal> selectActive();
}
