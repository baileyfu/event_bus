package io.github.eventbus.core;

import io.github.eventbus.exception.EventbusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-08-31 16:13
 * @description
 */
public class EventBusBroadcaster {
    private static Logger LOGGER = LoggerFactory.getLogger(EventBusBroadcaster.class);
    private static EventBusBroadcaster INSTANCE;

    private EBPub ebpub;
    private boolean opening;
    EventBusBroadcaster(EBPub ebpub, boolean opening) {
        this.ebpub = ebpub;
        this.opening = opening;
        INSTANCE = this;
        if(!this.opening){
            LOGGER.warn("EventBusBroadcaster has already closed , you can not broadcast any event to EventBus!");
        }
    }

    public static boolean broadcast(String eventName) {
        return broadcast(eventName, null);
    }

    public static boolean broadcast(String eventName, Object message) {
        if (INSTANCE.opening) {
            try {
                INSTANCE.ebpub.emit(eventName,message);
                return true;
            } catch (EventbusException e) {
                LOGGER.error("BusBroadcaster.broadcast() error!", e);
            }
        }
        return false;
    }
}
