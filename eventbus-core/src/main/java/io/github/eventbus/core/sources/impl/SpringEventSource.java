﻿package io.github.eventbus.core.sources.impl;

import io.github.eventbus.core.sources.AutoConsumeEventSource;
import io.github.eventbus.core.sources.Event;
import io.github.eventbus.core.sources.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.support.AbstractApplicationContext;

import java.util.Map;

/**
 * 事件可能丢失,不可重复消费
 *
 * @author ALi
 * @version 1.0
 * @date 2022-09-01 15:15
 * @description
 */
public class SpringEventSource extends AutoConsumeEventSource implements ApplicationContextAware {
    private Logger logger = LoggerFactory.getLogger(SpringEventSource.class);
    private AbstractApplicationContext applicationContext;
    private Map<String, EventSource.EventConsumer> consumers;
    public SpringEventSource(String name) {
        super(name);
        applicationContext.addApplicationListener(new ApplicationListener<PayloadApplicationEvent>(){
            @Override
            public void onApplicationEvent(PayloadApplicationEvent applicationEvent) {
                Object payload = applicationEvent.getPayload();
                if (payload == null || !payload.getClass().isAssignableFrom(Event.class)) {
                    return;
                }
                if (consumers != null) {
                    Event event = (Event) applicationEvent.getPayload();
                    try{
                        EventSource.EventConsumer eventConsumer = consumers.get(event.getName());
                        eventConsumer.accept(event.getSourceTerminal(), event.getName(), event.getMessage());
                    } catch (Exception e) {
                        logger.error("SpringEventSource consume event '" + event.getName() + "' error!", e);
                    }
                }
            }
        });
    }

    @Override
    public void startConsume(Map<String, EventSource.EventConsumer> consumers) {
        this.consumers = consumers;
    }

    @Override
    public void stopConsume() {
        this.consumers = null;
    }

    @Override
    protected void save(Event event) throws Exception {
        applicationContext.publishEvent(event);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (AbstractApplicationContext) applicationContext;
    }
}
