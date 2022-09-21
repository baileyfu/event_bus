package io.github.eventbus.core;

import io.github.eventbus.core.monitor.ResourceMonitor;
import io.github.eventbus.core.terminal.Terminal;
import io.github.eventbus.exception.EventbusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Collection;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-02 09:31
 * @description
 */
public class EventBusListener{
    private static Logger LOGGER = LoggerFactory.getLogger(EventBusListener.class);
    private static EventBusListener INSTANCE;

    private EBSub ebsub;
    Collection<EventHandler> handlers;
    private boolean opening;
    private boolean started;

    EventBusListener(EBSub ebsub, Collection<EventHandler> handlers, boolean opening) {
        this.ebsub = ebsub;
        this.handlers = handlers;
        this.opening = opening;
        INSTANCE = this;
        ResourceMonitor.registerResource(new ResourceMonitor.Switch() {
            @Override
            public void doOn() throws Exception {
                start();
            }
            @Override
            public void doOff() throws Exception {
                stop();
            }
            @Override
            public String identify() {
                return EventBusListener.this.toString();
            }
        });
    }
    void start() throws EventbusException {
        if (started) {
            return;
        }
        if (opening) {
            if (handlers != null && handlers.size() > 0) {
                handlers.forEach(EventBusListener::listen);
            }
            ebsub.start();
            started = true;
            LOGGER.info("EventBusListener is running!");
        }else{
            LOGGER.warn("EventBusListener has already closed , you will could not listen any event from EventBus!");
        }
    }
    void stop() {
        if (started) {
            ebsub.stop();
            started = false;
            LOGGER.info("EventBusListener is stopped!");
        }
    }

    public static void listen(EventHandler handler) {
        String eventName = handler.targetEventName();
        if (INSTANCE == null || INSTANCE.ebsub == null) {
            LOGGER.warn("EventBusListener is not initialized , you can not listen '" + eventName + "' from EventBus!");
            return;
        }
        if (!INSTANCE.started) {
            LOGGER.warn("EventBusListener is not started , you can not listen '" + eventName + "' from EventBus!");
            return;
        }
        if (!INSTANCE.opening) {
            LOGGER.warn("EventBusListener has already closed , you can not listen '" + eventName + "' from EventBus!");
            return;
        }
        Assert.hasLength(eventName, "EventHandler's targetEventName can not be empty!");
        INSTANCE.ebsub.listen(eventName, handler);
        if (UniqueEventHandler.class.isAssignableFrom(handler.getClass())) {
            INSTANCE.ebsub.setUniqueEventHandler(handler);
        }
    }

    /**
     * 订阅事件的处理器
     */
    public interface EventHandler{
        /**
         * 目标事件
         * @return
         */
        String targetEventName();

        /**
         * 对事件的处理;若未匹配到targetEventName()返回的事件名,则此方法不会被触发
         * @param sourceTerminal
         * @param eventName
         * @param message
         */
        void handle(Terminal sourceTerminal, String eventName, Object message);
    }

    /**
     * 负责处理所有事件
     */
    public interface UniqueEventHandler extends EventHandler {
        String MATCH_ALL_EVENT = "*";
        default String targetEventName() {
            return MATCH_ALL_EVENT;
        }
    }
}
