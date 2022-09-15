package io.github.eventbus.core.monitor;

import io.github.eventbus.core.EventBusListener;
import io.github.eventbus.exception.EventbusException;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

/**
 * Springboot资源管理器<br/>
 * @author ALi
 * @version 1.0
 * @date 2022-09-13 10:55
 * @description
 */
public class SpringbootResourceMonitor implements ApplicationListener<ApplicationStartedEvent> {
    private EventBusListener eventBusListener;

    public SpringbootResourceMonitor(EventBusListener eventBusListener) {
        this.eventBusListener = eventBusListener;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
        try {
            eventBusListener.start();
        } catch (EventbusException e) {
            throw new RuntimeException(e);
        }
    }
}
