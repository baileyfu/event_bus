package io.github.eventbus;

import io.github.eventbus.sources.EventSource;
import org.springframework.util.Assert;

import java.util.function.Consumer;

/**
 * 事件总线接收器
 *
 * @author ALi
 * @version 1.0
 * @date 2022-05-26 13:56
 * @description
 */
public class EBSub extends BusTemplate {
    private EventSource source;
    private Consumer<Object> doNothingHandler;

    public EBSub(String busName, EventSource source) {
        super(busName,source);
        doNothingHandler = (message) -> {};
    }

    public void listen(String eventName) {
        listen(eventName, doNothingHandler);
    }

    public void listen(String eventName, Consumer<Object> eventHandler) {
        Assert.hasLength(eventName,"'eventName' can not be empty.");
        Assert.notNull(eventHandler,"'eventHandler' can not be null.");

    }
}
