package io.github.eventbus.core.sources;

import io.github.eventbus.core.terminal.Terminal;
import io.github.eventbus.exception.EventbusException;

import java.util.function.Function;

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

    /**
     * 开启事件处理
     * @param consumerGetter
     * @return
     * @throws EventbusException
     */
    void consume(Function<String, EventConsumer> consumerGetter) throws EventbusException;

    /**
     * 事件源停机
     */
    void halt() ;

    interface EventConsumer {
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
}
