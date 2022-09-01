package io.github.eventbus.core.sources;

import io.github.eventbus.exception.EventbusException;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-05-26 16:07
 * @description
 */
public interface EventSource {
    public String getName();

    public void push(String eventName, Object message) throws EventbusException;

    public Object pop(String eventName) throws EventbusException;
}
