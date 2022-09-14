package io.github.eventbus.core.sources.impl;

import io.github.eventbus.core.sources.impl.database.dao.TopicalEventDAO;
import io.github.eventbus.core.sources.impl.database.dao.TopicalEventTerminalDAO;
import io.github.eventbus.core.terminal.Terminal;
import io.github.eventbus.util.IDGenerator;

/**
 * 发布-订阅型(Topic)-事件发给所有订阅的Terminal，可以被Terminal集群节点中的每一个节点消费一次<br/>
 * 确保事件被正常消费,消费失败可重复
 *
 * @author ALi
 * @version 1.0
 * @date 2022-09-07 11:16
 * @description
 */
public class DatabaseTopicEventClusterSource extends DatabaseTopicEventSource{
    public DatabaseTopicEventClusterSource(String name, TopicalEventDAO topicalEventDAO, TopicalEventTerminalDAO topicalEventTerminalDAO) {
        super(name, topicalEventDAO, topicalEventTerminalDAO);
    }

    @Override
    protected String createCurrentTerminalId(Terminal terminal) {
        return IDGenerator.generateTerminalId(terminal);
    }
}
