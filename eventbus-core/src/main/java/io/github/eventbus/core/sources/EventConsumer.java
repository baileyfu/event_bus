package io.github.eventbus.core.sources;

import io.github.eventbus.core.terminal.Terminal;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-10-08 10:13
 * @description
 */
public interface EventConsumer {
    /**
     * 消费失败则抛出异常,事件源回滚事件状态以再次消费(由具体事件源实现决定)
     * @param eventSourceName
     * @param sourceTerminal
     * @param eventName
     * @param message
     * @return 是否正常消费（不同于消费失败,比如被过滤）
     * @throws Exception
     */
    boolean accept(String eventSourceName, Terminal sourceTerminal, String eventName, Object message) throws Exception;
}