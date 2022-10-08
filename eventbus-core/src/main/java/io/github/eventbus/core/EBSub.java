package io.github.eventbus.core;

import io.github.eventbus.core.sources.EventConsumer;
import io.github.eventbus.core.sources.EventSource;
import io.github.eventbus.core.sources.filter.SubFilterChain;
import io.github.eventbus.exception.EventbusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Function;

/**
 * 事件总线-订阅/消费
 *
 * @author ALi
 * @version 1.0
 * @date 2022-05-26 13:56
 * @description
 */
public class EBSub implements SubFilterChain.ListenedFilterChangingListener {
    private Logger logger = LoggerFactory.getLogger(EBSub.class);
    private EventConsumer noMatchedConsumer = (eventSourceName, sourceTerminal, eventName, message) -> {
        if(logger.isDebugEnabled()){
            logger.debug(">>>---Eventbus received event '" + eventName + "' with message '"+message+"' from '"+sourceTerminal + "' on Eventsource '" + eventSourceName + "' , but no matched EventConsumer found.");
        }
        return false;
    };
    private EventConsumer filteredConsumer = (eventSourceName, sourceTerminal, eventName, message) -> {
        if(logger.isDebugEnabled()){
            logger.debug(">>>---Eventbus received event '" + eventName + "' with message '"+message+"' from '"+sourceTerminal + "' on Eventsource '" + eventSourceName + "' , but it has been filtered.");
        }
        return false;
    };
    private Collection<EventSource> sources;
    private Collection<ListenedEventChangingListener> eventChangingListeners;
    private final Map<String, EventConsumer> consumerMap;
    private HashSet<String> filteredEvent;
    private EventConsumer uniqueEventConsumer;
    //封装对consumerMap的操作
    private Function<String, EventConsumer> consumerGetter;
    private final SubFilterChain subFilterChain;

    EBSub(Collection<EventSource> sources, SubFilterChain subFilterChain) {
        Assert.isTrue(sources != null && sources.size() > 0,"the EBSub has no EventSource!");
        this.sources = sources;
        this.consumerMap = new HashMap<>();
        this.filteredEvent = new HashSet<>();
        this.consumerGetter = (eventName) -> {
            if (filteredEvent.contains(eventName)) {
                return filteredConsumer;
            }
            EventConsumer consumer = consumerMap.get(eventName);
            return consumer == null ? noMatchedConsumer : uniqueEventConsumer != null ? uniqueEventConsumer : consumer;
        };
        this.subFilterChain = subFilterChain;
        this.subFilterChain.registerFilterChangingListener(this);
    }
    void start() throws EventbusException{
        for (EventSource eventSource : sources) {
            if (eventSource instanceof ListenedEventChangingListener) {
                eventChangingListeners = eventChangingListeners == null ? new ArrayList<>() : eventChangingListeners;
                eventChangingListeners.add((ListenedEventChangingListener) eventSource);
            }
            eventSource.consume(consumerGetter);
        }
        invokeListenedEventChangingListener();
    }
    void stop(){
        for (EventSource eventSource : sources) {
            try {
                eventSource.halt();
            } catch (Exception e) {
                logger.error("EBSub stop EventSource named '" + eventSource.getName() + "' error!", e);
            }
        }
    }
    //当设置了uniqueEventHandler后,consumerMap将被忽略,所有事件都由uniqueEventConsumer处理
    void setUniqueEventHandler(EventBusListener.EventHandler uniqueEventHandler){
        Assert.notNull(uniqueEventHandler, "the UniqueEventHandler can not be null !!!");
        this.uniqueEventConsumer = handlerConvertToConsumer(uniqueEventHandler);
    }
    void listen(String eventName, EventBusListener.EventHandler eventHandler) {
        Assert.hasLength(eventName, "'eventName' can not be empty.");
        Assert.notNull(eventHandler, "'eventHandler' can not be null.");
        consumerMap.put(eventName, handlerConvertToConsumer(eventHandler));
        if (!subFilterChain.doFilter(eventName)) {
            filteredEvent.add(eventName);
        }
        invokeListenedEventChangingListener();
    }
    private EventConsumer handlerConvertToConsumer(EventBusListener.EventHandler eventHandler){
        return (eventSourceName, sourceTerminal, eventName, message) -> {
            eventHandler.handle(sourceTerminal, eventName, message);
            if(logger.isDebugEnabled()){
                logger.debug(">>>+++EBSub.EventConsumer for '" + eventSourceName + "' has consumed the event '"+eventName+"' with message '"+message+"' from '"+sourceTerminal+"'");
            }
            return true;
        };
    }

    /**
     * 获取SubFilterChain调用updateFilters()方法来热更新过滤规则
     * @return
     */
    SubFilterChain getSubFilterChain() {
        return subFilterChain;
    }

    @Override
    public void notifyCausedByFilterChanging() {
        HashSet<String> filteredEvent = new HashSet<>();
        for (String eventName : consumerMap.keySet()) {
            if (!subFilterChain.doFilter(eventName)) {
                filteredEvent.add(eventName);
            }
        }
        this.filteredEvent = filteredEvent;
    }

    private void invokeListenedEventChangingListener() {
        if (eventChangingListeners != null && eventChangingListeners.size() > 0) {
            List<String> listenedEvents = new ArrayList<>(consumerMap.keySet());
            for (ListenedEventChangingListener eventChangingListener : eventChangingListeners) {
                try {
                    eventChangingListener.notifyCausedByListenedEventChanging(listenedEvents);
                } catch (Exception e) {
                    logger.error("ListenedEventChangingListener.update() error!", e);
                }
            }
        }
    }
    /**
     * 订阅事件变动监听
     */
    public interface ListenedEventChangingListener {
        void notifyCausedByListenedEventChanging(List<String> listenedEvents);
    }
}
