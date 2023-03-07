package io.github.eventbus.core.monitor;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Spring资源管理器<br/>
 * @author ALi
 * @version 1.0
 * @date 2022-09-13 10:55
 * @description
 */
public class SpringResourceMonitor extends ResourceMonitor implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        doStart();
    }
}
