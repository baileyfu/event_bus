package io.github.eventbus.core;

import io.github.eventbus.core.sources.EventSource;
import org.springframework.util.Assert;

import java.util.List;
import java.util.function.Consumer;

/**
 * 事件总线接收器
 *
 * @author ALi
 * @version 1.0
 * @date 2022-05-26 13:56
 * @description
 */
public class EBSub{
    private List<EventSource> sources;
    private Consumer<Object> doNothingHandler;

    public EBSub(List<EventSource> sources) {
        Assert.noNullElements(sources, "the EBSub has no EventSource!");
        this.sources = sources;
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
