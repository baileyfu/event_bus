package io.github.eventbus.core.sources.impl;

import io.github.eventbus.core.sources.Event;
import io.github.eventbus.core.sources.ManualConsumeEventSource;
import io.github.eventbus.exception.EventbusException;

import java.util.function.Function;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-01 15:44
 * @description
 */
public class DatabaseEventSource extends ManualConsumeEventSource {
    public DatabaseEventSource(String name) {
        super(name);
    }

    @Override
    protected void save(Event event) throws Exception {
        //TODO
    }

    @Override
    public int consume(Function<String, EventConsumer> consumerGetter) throws EventbusException {
        return 0;
    }
}
