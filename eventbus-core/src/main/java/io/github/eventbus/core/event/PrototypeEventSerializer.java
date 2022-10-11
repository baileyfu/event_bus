package io.github.eventbus.core.event;

import io.github.eventbus.exception.EventbusException;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-10-11 16:43
 * @description
 */
public class PrototypeEventSerializer implements EventSerializer<Event>{
    private static PrototypeEventSerializer INSTANCE;
    public static PrototypeEventSerializer getInstance(){
        if (INSTANCE == null) {
            INSTANCE = new PrototypeEventSerializer();
        }
        return INSTANCE;
    }
    @Override
    public Event serialize(Event event) throws EventbusException {
        return event;
    }

    @Override
    public Event deserialize(Event serialized) throws EventbusException {
        return serialized;
    }
}
