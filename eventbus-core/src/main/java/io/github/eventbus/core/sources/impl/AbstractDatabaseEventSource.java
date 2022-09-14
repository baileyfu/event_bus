package io.github.eventbus.core.sources.impl;

import com.alibaba.fastjson.JSON;
import io.github.ali.commons.variable.MixedActionGenerator;
import io.github.eventbus.constants.EventSourceConfigConst;
import io.github.eventbus.constants.JSONConfig;
import io.github.eventbus.core.sources.Event;
import io.github.eventbus.core.sources.ManualConsumeEventSource;
import io.github.eventbus.core.terminal.Terminal;
import io.github.eventbus.exception.EventbusException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-07 17:42
 * @description
 */
public abstract class AbstractDatabaseEventSource extends ManualConsumeEventSource {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    protected int limit;
    protected Boolean cleaningRequired;
    protected int cleanCycle;
    public AbstractDatabaseEventSource(String name) {
        super(name);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        if (limit == 0) {
            setLimit(Integer.valueOf(environment.getProperty(EventSourceConfigConst.CONSUME_LIMIT, "100")));
        }
        if (cleanCycle == 0) {
            setCleanCycle(Integer.valueOf(environment.getProperty(EventSourceConfigConst.CLEAN_CYCLE, "1")));
        }
        if (cleaningRequired == null) {
            setCleaningRequired(Boolean.valueOf(environment.getProperty(EventSourceConfigConst.CLEAN_REQUIRED, "true")));
        }
        if (cleaningRequired) {
            //启动定时(30分钟)清理
            String actionName = this + ".clean";
            MixedActionGenerator.loadAction(actionName, 30, TimeUnit.MINUTES, () -> {
                try{
                    clean();
                } catch (Exception e) {
                    logger.error("the action of " + actionName + ".clean() error!", e);
                }
            });
        }
    }

    public void setLimit(int limit) {
        this.limit = limit;
        if (this.limit < 1) {
            this.limit = 1;
            logger.warn(EventSourceConfigConst.CONSUME_LIMIT + " value is " + this.limit + " , reset to 1.");
        }
    }

    public void setCleanCycle(int cleanCycle) {
        this.cleanCycle = cleanCycle;
        if (this.cleanCycle < 1) {
            this.cleanCycle = 1;
            logger.warn(EventSourceConfigConst.CLEAN_CYCLE + " value is " + this.cleanCycle + " , reset to 1.");
        }
    }

    public void setCleaningRequired(boolean cleaningRequired) {
        this.cleaningRequired = cleaningRequired;
    }

    @Override
    public int consume(Function<String, EventConsumer> consumerGetter) throws EventbusException {
        int consumedCount = 0;
        try{
            Map<Long, Event> waitingEvents = fetchAndSetUnconsumed();
            if (waitingEvents != null && waitingEvents.size() > 0) {
                for (Long eventId : waitingEvents.keySet()) {
                    Event event = waitingEvents.get(eventId);
                    EventConsumer eventConsumer = consumerGetter.apply(event.getName());
                    try {
                        eventConsumer.accept(this.getName(), event.getSourceTerminal(), event.getName(), event.getMessage());
                        consumedCount++;
                    } catch (Exception e) {
                        logger.error("DatabaseEventSource consume error with '" + event + "'!", e);
                        //单个事件消费失败不影响其他事件的消费
                        rollback(eventId);
                    }
                }
            }
        } catch (Exception e) {
            throw new EventbusException("DatabaseEventSource.consume() error !", e);
        }
        return consumedCount;
    }

    /**
     * 将事件状态重制为unconsumed
     * @param eventId
     */
    protected void rollback(long eventId) {
        try {
            setUnconsumed(eventId);
        } catch (Exception e) {
            logger.error("DatabaseEventSource rollback error with '" + eventId + "'!", e);
            //TODO 写入指定日志让人工回滚
        }
    }

    protected String serializeMessage(Object message) {
        return message == null ? StringUtils.EMPTY : JSON.toJSONString(message, JSONConfig.SERIALIZER_FEATURE_ARRAY);
    }
    protected Object deserializeMessage(String jsonMessage,String messageTypeValue) throws EventbusException{
        try{
            return StringUtils.isEmpty(jsonMessage) ? null : JSON.parseObject(jsonMessage,Class.forName(messageTypeValue), JSONConfig.FEATURE_ARRAY);
        }catch(Exception e){
            throw new EventbusException("AbstractDatabaseEventSource.deserializeMessage() error!", e);
        }
    }
    protected String serializeTerminal(Terminal sourceTerminal) {
        return sourceTerminal == null ? StringUtils.EMPTY : JSON.toJSONString(sourceTerminal);
    }
    protected Terminal deserializeTerminal(String jsonSourceTerminal) {
        return StringUtils.isEmpty(jsonSourceTerminal) ? null : JSON.parseObject(jsonSourceTerminal, Terminal.class);
    }

    abstract protected Map<Long,Event> fetchAndSetUnconsumed() throws Exception;
    abstract protected void setUnconsumed(long eventId) throws Exception;
    abstract protected void clean() throws Exception;
}
