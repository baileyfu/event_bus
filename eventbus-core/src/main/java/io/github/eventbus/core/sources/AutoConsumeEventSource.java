package io.github.eventbus.core.sources;

import io.github.eventbus.exception.EventbusException;

import java.util.Map;

/**
 * 由具体依赖的消息队列拉取
 * @author ALi
 * @version 1.0
 * @date 2022-09-05 09:38
 * @description
 */
public abstract class AutoConsumeEventSource extends AbstractEventSource{
    private Map<String, EventConsumer> consumers;
    public AutoConsumeEventSource(String name) {
        super(name);
    }

    @Override
    public int consume(Map<String, EventConsumer> consumers) throws EventbusException {
        //do nothing,the startConsume() do the job
        return 0;
    }

    /**
     * 启动消费,需对事件消费的异常进行处理以防止中断消费线程
     * @param consumers
     */
    public abstract void startConsume(Map<String, EventSource.EventConsumer> consumers);

    /**
     * 停止消费
     */
    public abstract void stopConsume();
}
