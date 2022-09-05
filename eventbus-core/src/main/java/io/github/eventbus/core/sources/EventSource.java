package io.github.eventbus.core.sources;

import io.github.eventbus.core.terminal.Terminal;
import io.github.eventbus.exception.EventbusException;

import java.util.Map;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-05-26 16:07
 * @description
 */
public interface EventSource {
    /**
     * 事件源名称,唯一
     * @return
     */
    String getName();

    /**
     * 事件入栈
     * @param eventName
     * @param message
     * @throws EventbusException
     */
    void push(String eventName, Object message) throws EventbusException;

    int consume(Map<String, EventConsumer> consumers) throws EventbusException;

    interface EventConsumer {
        void accept(Terminal sourceTerminal, String eventName, Object message);
    }
}
