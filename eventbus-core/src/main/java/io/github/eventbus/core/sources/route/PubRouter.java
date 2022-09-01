package io.github.eventbus.core.sources.route;

import io.github.eventbus.core.sources.EventSource;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-08-31 18:27
 * @description
 */
public interface PubRouter {
    /**
     *
     * @param eventName
     * @return 事件源名称
     */
    public String[] route(String eventName);

    /**
     * 优先级;值越小优先级越高
     * @return
     */
    default public int getPriority(){
        return Integer.MAX_VALUE;
    };
}
