package io.github.eventbus.core.monitor;

import io.github.ali.commons.variable.MixedActionGenerator;
import io.github.eventbus.core.EventBusListener;
import io.github.eventbus.exception.EventbusException;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.annotation.PreDestroy;

/**
 * 资源管理器<br/>
 * 框架启动时申请的各种资源由该类负责启动/释放
 * @author ALi
 * @version 1.0
 * @date 2022-09-14 10:55
 * @description
 */
public class ResourceMonitor implements ApplicationListener<ContextRefreshedEvent> {
    private EventBusListener eventBusListener;
    public ResourceMonitor(EventBusListener eventBusListener) {
        this.eventBusListener = eventBusListener;
    }

    /**
     * SpringBoot需要显示调用ConfigurableApplicationContext.start()才会出发ContextRefreshedEvent事件<br/>
     * @param contextRefreshedEvent
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        try {
            eventBusListener.start();
        } catch (EventbusException e) {
            throw new RuntimeException(e);
        }
    }
    @PreDestroy
    public void stop(){
        try {
            eventBusListener.stop();
            MixedActionGenerator.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
