package io.github.eventbus.core.monitor;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

/**
 * Springboot资源管理器<br/>
 *
 * @author ALi
 * @version 1.0
 * @date 2022-09-13 10:55
 * @description
 */
public class SpringbootResourceMonitor extends ResourceMonitor implements ApplicationListener<ApplicationStartedEvent>, DisposableBean {
    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
        doStart();
    }

    @Override
    public void destroy() throws Exception {
        doStop();
    }
}
