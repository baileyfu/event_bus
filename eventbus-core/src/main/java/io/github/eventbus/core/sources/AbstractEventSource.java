package io.github.eventbus.core.sources;

import io.github.eventbus.constants.EventSourceConfigConst;
import io.github.eventbus.core.event.Event;
import io.github.eventbus.core.event.EventSerializer;
import io.github.eventbus.core.terminal.TerminalFactory;
import io.github.eventbus.exception.EventbusException;
import io.github.eventbus.core.event.JSONEventSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.util.function.Function;


/**
 * 事件序列化默认是JSONEventSerializer
 * @author ALi
 * @version 1.0
 * @date 2022-06-01 17:33
 * @description
 */
public abstract class AbstractEventSource implements EventSource, InitializingBean, EnvironmentAware {
    public static final int DEFAULT_CONSUME_LIMIT = 10;
    public static final int MIN_CONSUME_LIMIT = 1;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    protected Environment environment;
    //单次消费数量
    protected int consumeLimit;

    private String name;
    private boolean isMarching;
    private boolean isConsuming;
    private EventSerializer eventSerializer;

    public AbstractEventSource(String name) {
        Assert.hasLength(name, "the " + this + "'name can not be empty!");
        this.name = name;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public void setEventSerializer(EventSerializer eventSerializer) {
        this.eventSerializer = eventSerializer;
    }

    public EventSerializer getEventSerializer() {
        return eventSerializer;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (consumeLimit < MIN_CONSUME_LIMIT) {
            setConsumeLimit(Integer.valueOf(environment.getProperty(EventSourceConfigConst.CONSUME_LIMIT, String.valueOf(DEFAULT_CONSUME_LIMIT))));
        }
        //未设置序列化则使用默认
        eventSerializer = eventSerializer == null ? JSONEventSerializer.getInstance() : eventSerializer;
        isMarching = true;
        isConsuming = false;
    }
    public void setConsumeLimit(int consumeLimit) {
        this.consumeLimit = consumeLimit;
        if (this.consumeLimit < MIN_CONSUME_LIMIT) {
            this.consumeLimit = DEFAULT_CONSUME_LIMIT;
            logger.warn(EventSourceConfigConst.CONSUME_LIMIT + " value is " + consumeLimit + " , reset to " + DEFAULT_CONSUME_LIMIT);
        }
    }
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void push(String eventName, Object message) throws EventbusException {
        if (!isMarching) {
            throw new EventbusException("EventSource " + name + " was halted!");
        }
        Event event = Event.EventBuilder.newInstance()
                .name(eventName)
                .message(message)
                .sourceTerminal(TerminalFactory.create())
                .build();
        try {
            save(eventName, eventSerializer.serialize(event));
        } catch (Exception e) {
            throw new EventbusException("EventSource push error!", e);
        }
    }
    abstract protected void save(String eventName, Object serializedEvent) throws Exception;

    @Override
    public void consume(Function<String, EventConsumer> consumerGetter) throws EventbusException {
        if (!isMarching) {
            throw new EventbusException("EventSource " + name + " is halted!");
        }
        synchronized (this) {
            if (!isConsuming) {
                try {
                    startConsume((serializedEvent) -> {
                        try {
                            Event event = eventSerializer.deserialize(serializedEvent);
                            return consumerGetter.apply(event.getName()).accept(name, event.getSourceTerminal(), event.getName(), event.getMessage());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                    isConsuming = true;
                } catch (Exception e) {
                    throw new EventbusException("EventSource consume error!", e);
                }
            }
        }
    }
    @Override
    public void halt() {
        if (!isMarching) {
            logger.warn("EventSource " + name + " is already halted , call halt() will be ignored.");
        } else {
            synchronized (this) {
                try {
                    stopConsume();
                    isConsuming = false;
                    isMarching = false;
                } catch (Exception e) {
                    logger.error("EventSource halt error!", e);
                }
            }
        }
    }
    /**
     * 启动消费,需对事件消费的异常进行处理以防止中断消费线程
     * @param consumer
     */
    abstract protected void startConsume(Function<Object, Boolean> consumer);
    abstract protected void stopConsume();
}
