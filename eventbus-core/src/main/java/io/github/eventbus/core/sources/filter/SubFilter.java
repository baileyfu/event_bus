package io.github.eventbus.core.sources.filter;

/**
 * 事件过滤器-决定那些事件能被当前节点消费
 * @author ALi
 * @version 1.0
 * @date 2022-09-05 16:36
 * @description
 */
public interface SubFilter {
    boolean doFilter(String eventName);
}
