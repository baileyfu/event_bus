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

    public abstract void startConsume(Map<String, EventSource.EventConsumer> consumers);

    public abstract void stopConsume();
}
