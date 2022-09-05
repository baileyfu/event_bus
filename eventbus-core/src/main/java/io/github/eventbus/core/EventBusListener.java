﻿package io.github.eventbus.core;

import io.github.eventbus.core.terminal.Terminal;
import io.github.eventbus.exception.EventbusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-02 09:31
 * @description
 */
public class EventBusListener {
    private static Logger LOGGER = LoggerFactory.getLogger(EventBusListener.class);

    private EBSub ebsub;
    List<EventHandler> handlers;
    private boolean opening;
    private boolean started;
    EventBusListener(EBSub ebsub, List<EventHandler> handlers, boolean opening) {
        this.ebsub = ebsub;
        this.handlers = handlers;
        this.opening = opening;
    }
    @PostConstruct
    public void start() throws EventbusException {
        if (opening) {
            if (handlers != null && handlers.size() > 0) {
                for (EventHandler handler : handlers) {
                    Assert.hasLength(handler.targetEventName(), "EventHandler's targetEventName can not be empty!");
                    ebsub.listen(handler.targetEventName(), handler);
                    if (handler.getClass().isAssignableFrom(UniqueEventHandler.class)) {
                        ebsub.setUniqueEventHandler(handler);
                    }
                }
            }
            ebsub.start();
            LOGGER.info("EventBusListener is running!");
        }else{
            LOGGER.warn("EventBusListener has already closed , you can not listen any event from EventBus!");
        }
        started = true;
    }
    @PreDestroy
    public void stop() {
        if (started) {
            ebsub.stop();
            started = false;
            LOGGER.info("EventBusListener is stopped!");
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
