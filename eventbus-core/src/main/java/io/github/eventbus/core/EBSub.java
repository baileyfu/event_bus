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
public class EBSub {
    private Logger logger = LoggerFactory.getLogger(EBSub.class);
    private EventSource.EventConsumer doNothingHandler = (eventSourceName, sourceTerminal, eventName, message) -> {
        if(logger.isDebugEnabled()){
            logger.debug("EBSub.doNothingHandler for '" + eventSourceName + "' consumed event '" + eventName + "' from '"+sourceTerminal+"'");
        }
    };
    private Collection<EventSource> sources;
    private Collection<ListenedEventChangingListener> eventChangingListeners;
    private final Map<String, EventSource.EventConsumer> consumerMap;
    //封装对consumerMap的操作
    private Function<String, EventSource.EventConsumer> consumerGetter;
    private SubFilterChain subFilterChain;

    EBSub(Collection<EventSource> sources, SubFilterChain subFilterChain) {
        Assert.noNullElements(sources, "the EBSub has no EventSource!");
        this.sources = sources;
        this.consumerMap = new HashMap<>();
        this.consumerGetter = (eventName) -> consumerMap.getOrDefault(eventName, doNothingHandler);
        this.subFilterChain = subFilterChain;
    }
    void start() throws EventbusException{
        List<String> listenedEvents = new ArrayList<>(consumerMap.keySet());
        for (EventSource eventSource : sources) {
            if (eventSource instanceof ListenedEventChangingListener) {
                eventChangingListeners = eventChangingListeners == null ? new ArrayList<>() : eventChangingListeners;
                ListenedEventChangingListener listenedEventChangingListener = (ListenedEventChangingListener) eventSource;
                listenedEventChangingListener.update(listenedEvents);
                eventChangingListeners.add(listenedEventChangingListener);
            }
            if (eventSource instanceof AutoConsumeEventSource) {
                ((AutoConsumeEventSource) eventSource).startConsume(consumerGetter);
            } else if (eventSource instanceof ManualConsumeEventSource) {
                startConsume((ManualConsumeEventSource) eventSource);
            } else {
                throw new EventbusException(String.format("No supported EventSource type '%s' , the EventSource must extends from ManualConsumeEventSource.class or AutoConsumeEventSource.class",eventSource.getClass()));
            }
        }
    }
    private void startConsume(ManualConsumeEventSource manualConsumeEventSource){
        MixedActionGenerator.loadAction(manualConsumeEventSource.getName(),manualConsumeEventSource.getConsumeInterval(),TimeUnit.MILLISECONDS,()->{
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
                    MixedActionGenerator.unloadAction(eventSource.getName(),false);
                }
            } catch (Exception e) {
                logger.error("EBSub stop EventSource named '" + eventSource.getName() + "' error!", e);
            }
        }
    }
    //当设置了uniqueEventHandler后,consumerMap将被忽略,所有事件都由uniqueEventConsumer处理
    void setUniqueEventHandler(EventBusListener.EventHandler uniqueEventHandler){
        EventSource.EventConsumer uniqueEventConsumer = handlerConvertToConsumer(uniqueEventHandler);
        consumerGetter = (eventName) -> uniqueEventConsumer;
    }
    void listen(String eventName, EventBusListener.EventHandler eventHandler) {
        Assert.hasLength(eventName, "'eventName' can not be empty.");
        Assert.notNull(eventHandler, "'eventHandler' can not be null.");
        consumerMap.put(eventName, handlerConvertToConsumer(eventHandler));
        if (eventChangingListeners != null) {
            List<String> listenedEvents = new ArrayList<>(consumerMap.keySet());
            eventChangingListeners.stream().forEach((eventChangingListener) -> eventChangingListener.update(listenedEvents));
        }
    }
    private EventSource.EventConsumer handlerConvertToConsumer(EventBusListener.EventHandler eventHandler){
        return (eventSourceName, sourceTerminal, eventName, message) -> {
            if (subFilterChain.doFilter(eventName)) {
                eventHandler.handle(sourceTerminal, eventName, message);
                if(logger.isDebugEnabled()){
                    logger.debug(">>>+++EBSub.EventConsumer for '" + eventSourceName + "' has consumed the event '"+eventName+"' from '"+sourceTerminal+"'");
                }
            } else {
                if(logger.isDebugEnabled()){
                    logger.debug(">>>---EBSub.EventConsumer for '" + eventSourceName + "' has filtered the event '"+eventName+"' from '"+sourceTerminal+"'");
                }
            }
        };
    }

    /**
     * 订阅事件变动监听
     */
    public interface ListenedEventChangingListener {
        void update(List<String> listenedEvents);
    }
}
