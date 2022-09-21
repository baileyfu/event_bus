package io.github.eventbus.core;

import io.github.ali.commons.variable.MixedActionGenerator;
import io.github.eventbus.core.sources.AutoConsumeEventSource;
import io.github.eventbus.core.sources.EventSource;
import io.github.eventbus.core.sources.ManualConsumeEventSource;
import io.github.eventbus.core.sources.filter.SubFilterChain;
import io.github.eventbus.exception.EventbusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.TimeUnit;
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
    private EventSource.EventConsumer noMatchedHandler = (eventSourceName, sourceTerminal, eventName, message) -> {
        if(logger.isDebugEnabled()){
            logger.debug(">>>---Eventbus received event '" + eventName + "' with message '"+message+"' from '"+sourceTerminal + "' on Eventsource '" + eventSourceName + "' , but no matched EventConsumer found.");
        }
        return false;
    };
    private EventSource.EventConsumer filteredHandler = (eventSourceName, sourceTerminal, eventName, message) -> {
        if(logger.isDebugEnabled()){
            logger.debug(">>>---Eventbus received event '" + eventName + "' with message '"+message+"' from '"+sourceTerminal + "' on Eventsource '" + eventSourceName + "' , but it has been filtered.");
        }
        return false;
    };
    private Collection<EventSource> sources;
    private Collection<ListenedEventChangingListener> eventChangingListeners;
    private final Map<String, EventSource.EventConsumer> consumerMap;
    private HashSet<String> filteredEvent;
    private EventSource.EventConsumer uniqueEventConsumer;
    //封装对consumerMap的操作
    private Function<String, EventSource.EventConsumer> consumerGetter;
    private final SubFilterChain subFilterChain;

    EBSub(Collection<EventSource> sources, SubFilterChain subFilterChain) {
        Assert.isTrue(sources != null && sources.size() > 0,"the EBSub has no EventSource!");
        this.sources = sources;
        this.consumerMap = new HashMap<>();
        this.filteredEvent = new HashSet<>();
        this.consumerGetter = (eventName) -> {
            if (filteredEvent.contains(eventName)) {
                return filteredHandler;
            }
            EventSource.EventConsumer consumer = consumerMap.get(eventName);
            return consumer == null ? noMatchedHandler : uniqueEventConsumer != null ? uniqueEventConsumer : consumer;
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
            if (eventSource instanceof AutoConsumeEventSource) {
                ((AutoConsumeEventSource) eventSource).startConsume(consumerGetter);
            } else if (eventSource instanceof ManualConsumeEventSource) {
                startConsume((ManualConsumeEventSource) eventSource);
            } else {
                throw new EventbusException(String.format("No supported EventSource type '%s' , the EventSource must extends from ManualConsumeEventSource.class or AutoConsumeEventSource.class",eventSource.getClass()));
            }
        }
        invokeListenedEventChangingListener();
    }
    private void startConsume(ManualConsumeEventSource manualConsumeEventSource){
        MixedActionGenerator.loadAction(generateActionName(manualConsumeEventSource),manualConsumeEventSource.getConsumeInterval(),TimeUnit.MILLISECONDS,()->{
            try {
                int consumed = manualConsumeEventSource.consume(consumerGetter);
                // 如果没消费到消息则暂停x毫秒
                if (manualConsumeEventSource.gePauseIfNotConsumed() > 0 && consumed == 0) {
                    Thread.sleep(manualConsumeEventSource.gePauseIfNotConsumed());
                }
            } catch (Exception e) {
                logger.error(manualConsumeEventSource.getName() + " consume error !", e);
                // 消费出错后暂停100ms
                try {
                    Thread.sleep(100l);
                } catch (InterruptedException ie) {
                    logger.error(manualConsumeEventSource.getName() + " sleeping after consuming failed error !", ie);
                }
            }
        });
    }
    void stop(){
        for (EventSource eventSource : sources) {
            try {
                if (eventSource instanceof AutoConsumeEventSource) {
                    ((AutoConsumeEventSource)eventSource).stopConsume();
                } else if (eventSource instanceof ManualConsumeEventSource) {
                    //由ResourceReleaser来负责最终的释放
                    MixedActionGenerator.unloadAction(generateActionName(eventSource),false);
                }
            } catch (Exception e) {
                logger.error("EBSub stop EventSource named '" + eventSource.getName() + "' error!", e);
            }
        }
    }
    private String generateActionName(EventSource eventSource) {
        return new StringBuilder()
                .append("Eventbus.EventSource.")
                .append(eventSource.getName())
                .append(".consuming")
                .toString();
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
    private EventSource.EventConsumer handlerConvertToConsumer(EventBusListener.EventHandler eventHandler){
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
