package io.github.eventbus.core.sources.impl;

import io.github.eventbus.core.event.Event;
import io.github.eventbus.core.event.PrototypeEventSerializer;
import io.github.eventbus.core.sources.AutoConsumeEventSource;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.support.AbstractApplicationContext;

import java.util.function.Function;

/**
 * 进程内的线程发布/消费-事件可能丢失,不可重复消费
 *
 * @author ALi
 * @version 1.0
 * @date 2022-09-01 15:15
 * @description
 */
public class SpringEventSource extends AutoConsumeEventSource implements ApplicationContextAware {
    private AbstractApplicationContext applicationContext;
    private boolean running;
    public SpringEventSource(String name) {
        super(name);
        running = false;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        setEventSerializer(PrototypeEventSerializer.getInstance());
    }

    @Override
    public void startConsume(Function<Object, Boolean> consumer) {
        applicationContext.addApplicationListener((ApplicationListener<PayloadApplicationEvent>) applicationEvent -> {
            if (running) {
                Object payload = applicationEvent.getPayload();
                if (payload == null || !Event.class.isAssignableFrom(payload.getClass())) {
                    return;
                }
                Object serializedEvent = applicationEvent.getPayload();
                try {
                    consumer.apply(serializedEvent);
                } catch (Exception e) {
                    logger.error("SpringEventSource consume event '" + serializedEvent + "' error!", e);
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
    protected void save(String eventName, Object serializedEvent) throws Exception {
        applicationContext.publishEvent(serializedEvent);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (AbstractApplicationContext) applicationContext;
    }
}
