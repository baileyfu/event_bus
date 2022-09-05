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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 事件总线-订阅/消费
 *
 * @author ALi
 * @version 1.0
 * @date 2022-05-26 13:56
 * @description
 */
public class EBSub {
    private static Logger LOGGER = LoggerFactory.getLogger(EBSub.class);
    private static EventSource.EventConsumer doNothingHandler = (sourceTerminal, eventName, message) -> {
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("doNothingHandler consumed event ? from ?", eventName, sourceTerminal);
        }
    };
    private List<EventSource> sources;
    private Map<String, EventSource.EventConsumer> consumerMap;
    private SubFilterChain subFilterChain;

    EBSub(List<EventSource> sources, SubFilterChain subFilterChain) {
        Assert.noNullElements(sources, "the EBSub has no EventSource!");
        this.sources = sources;
        this.consumerMap = new HashMap<String, EventSource.EventConsumer>() {
            @Override
            public EventSource.EventConsumer get(Object key) {
                return super.getOrDefault(key, doNothingHandler);
            }
        };
        this.subFilterChain = subFilterChain;
    }
    void start() throws EventbusException{
        for (EventSource eventSource : sources) {
            if (eventSource instanceof AutoConsumeEventSource) {
                ((AutoConsumeEventSource) eventSource).startConsume(consumerMap);
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
                int consumed = manualConsumeEventSource.consume(consumerMap);
                // 如果没消费到消息则暂停x毫秒
                if (manualConsumeEventSource.gePauseIfNotConsumed() > 0 && consumed == 0) {
                    Thread.sleep(manualConsumeEventSource.gePauseIfNotConsumed());
                }
            } catch (Exception e) {
                LOGGER.error(manualConsumeEventSource.getName() + " consume error !", e);
                // 消费出错后暂停100ms
                try {
                    Thread.sleep(100l);
                } catch (InterruptedException ie) {
                    LOGGER.error(manualConsumeEventSource.getName() + " sleeping after consuming failed error !", ie);
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
                    MixedActionGenerator.unloadAction(eventSource.getName(),true);
                }
            } catch (Exception e) {
                LOGGER.error("EBSub stop EventSource named '" + eventSource.getName() + "' error!", e);
            }
        }
    }
    //当设置了uniqueEventHandler后,consumerMap将被忽略,所有事件都由uniqueEventConsumer处理
    void setUniqueEventHandler(EventBusListener.EventHandler uniqueEventHandler){
        EventSource.EventConsumer uniqueEventConsumer = handlerConvertToConsumer(uniqueEventHandler);
        this.consumerMap = new HashMap<String, EventSource.EventConsumer>() {
            @Override
            public EventSource.EventConsumer get(Object key) {
                return uniqueEventConsumer;
            }
        };
    }
    void listen(String eventName, EventBusListener.EventHandler eventHandler) {
        Assert.hasLength(eventName, "'eventName' can not be empty.");
        Assert.notNull(eventHandler, "'eventHandler' can not be null.");
        consumerMap.put(eventName, handlerConvertToConsumer(eventHandler));
    }
    private EventSource.EventConsumer handlerConvertToConsumer(EventBusListener.EventHandler eventHandler){
        return (terminal, eventName, message) -> {
            if (subFilterChain.doFilter(eventName)) {
                eventHandler.handle(terminal, eventName, message);
            }
        };
    }
}
