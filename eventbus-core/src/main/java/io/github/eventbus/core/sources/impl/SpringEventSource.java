package io.github.eventbus.core.sources.impl;

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

import java.util.function.Function;

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
    private boolean running;
    public SpringEventSource(String name) {
        super(name);
        running = false;
    }

    @Override
    public void startConsume(Function<String, EventConsumer> consumerGetter) {
        applicationContext.addApplicationListener((ApplicationListener<PayloadApplicationEvent>) applicationEvent -> {
            if(running){
                Object payload = applicationEvent.getPayload();
                if (payload == null || !Event.class.isAssignableFrom(payload.getClass())) {
                    return;
                }
                Event event = (Event) applicationEvent.getPayload();
                try {
                    EventConsumer eventConsumer = consumerGetter.apply(event.getName());
                    eventConsumer.accept(this.getName(), event.getSourceTerminal(), event.getName(), event.getMessage());
                } catch (Exception e) {
                    logger.error("SpringEventSource consume event '" + event.getName() + "' error!", e);
                }
            }
        });
        running = true;
    }

    @Override
    public void stopConsume() {
        running = false;
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
