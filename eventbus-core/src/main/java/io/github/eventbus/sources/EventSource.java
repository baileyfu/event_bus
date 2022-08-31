package io.github.eventbus.sources;

import io.github.eventbus.exception.EventsbusException;
import io.github.eventbus.terminal.Terminal;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-05-26 16:07
 * @description
 */
public interface EventSource {

    public void push(String eventName, Object message) throws EventsbusException;

    public Object pop(String eventName) throws EventsbusException;
}
