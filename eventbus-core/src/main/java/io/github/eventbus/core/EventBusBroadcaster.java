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
        if(!this.opening){
            LOGGER.warn("EventBusBroadcaster has already closed , you will could not broadcast any event to EventBus!");
        }
        INSTANCE = this;
    }

    /**
     * 发布消息
     * @param eventName 事件名称；全局唯一，推荐格式：系统名.服务名.业务领域名.具体事件名
     * @return
     */
    public static boolean broadcast(String eventName) {
        return broadcast(eventName, null);
    }

    /**
     * 发布消息
     * @param eventName 事件名称；全局唯一，推荐格式：系统名.服务名.业务领域名.具体事件名
     * @param message
     * @return
     */
    public static boolean broadcast(String eventName, Object message) {
        if (INSTANCE == null || INSTANCE.ebpub == null) {
            LOGGER.warn("EventBusBroadcaster is not initialized , you can not broadcast '" + eventName + "' to EventBus!");
            return false;
        }
        if (!INSTANCE.opening) {
            LOGGER.warn("EventBusBroadcaster has already closed , you can not broadcast '" + eventName + "' to EventBus!");
            return false;
        }
        try {
            INSTANCE.ebpub.emit(eventName,message);
        } catch (EventbusException e) {
            LOGGER.error("BusBroadcaster.broadcast event " + eventName + " with '" + message + "' error!", e);
            return false;
        }
        return true;
    }
}
