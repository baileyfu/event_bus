package io.github.eventbus.core.sources.impl;

import io.github.eventbus.core.sources.AbstractEventSource;
import io.github.eventbus.core.sources.Event;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-01 15:44
 * @description
 */
public class DatabaseEventSource extends AbstractEventSource {
    public DatabaseEventSource(String name) {
        super(name);
    }

    @Override
    protected void save(Event event) throws Exception {
        //TODO
    }
}
