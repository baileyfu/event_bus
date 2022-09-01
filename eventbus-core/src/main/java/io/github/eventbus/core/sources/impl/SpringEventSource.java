package io.github.eventbus.core.sources.impl;

import io.github.eventbus.core.sources.AbstractEventSource;
import io.github.eventbus.core.sources.Event;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-01 15:15
 * @description
 */
public class SpringEventSource extends AbstractEventSource implements ApplicationContextAware {
    private AbstractApplicationContext applicationContext;
    public SpringEventSource(String name) {
        super(name);
        applicationContext.addApplicationListener(new ApplicationListener<PayloadApplicationEvent>(){
            @Override
            public void onApplicationEvent(PayloadApplicationEvent applicationEvent) {
                Object payload = applicationEvent.getPayload();
                if (payload == null || !payload.getClass().isAssignableFrom(Event.class)) {
                    return;
                }
                Event event = (Event) applicationEvent.getPayload();
                //TODO
            }
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (AbstractApplicationContext) applicationContext;
    }

    @Override
    protected void save(Event event) throws Exception {
        applicationContext.publishEvent(event);
    }
}
