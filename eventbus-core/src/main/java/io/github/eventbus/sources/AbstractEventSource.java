package io.github.eventbus.sources;

import io.github.eventbus.exception.EventsbusException;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-06-01 17:33
 * @description
 */
public abstract class AbstractEventSource implements EventSource {

    public AbstractEventSource() {

    }

    @Override
    public void push(String eventName, Object message) throws EventsbusException {
    }

    @Override
    public Object pop(String eventName) throws EventsbusException {
        return null;
    }
}
