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
public class BusBroadcaster {
    private static Logger LOGGER = LoggerFactory.getLogger(BusBroadcaster.class);
    private static BusBroadcaster INSTANCE;

    private EBPub ebpub;
    private boolean opening;
    BusBroadcaster(EBPub ebpub, boolean opening) {
        this.ebpub = ebpub;
        this.opening = opening;
        INSTANCE = this;
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
