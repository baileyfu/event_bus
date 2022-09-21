package io.github.eventbus.core.sources;

import io.github.eventbus.constants.EventSourceConfigConst;
import io.github.eventbus.core.terminal.TerminalFactory;
import io.github.eventbus.exception.EventbusException;
import io.github.eventbus.util.JSONEventSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;


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
    protected Event.EventSerializer eventSerializer;
    //单次消费数量
    protected int consumeLimit;
    private String name;

    public AbstractEventSource(String name) {
        Assert.hasLength(name, "the EventSource'name can not be empty!");
        this.name = name;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public void setEventSerializer(Event.EventSerializer eventSerializer) {
        this.eventSerializer = eventSerializer;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (consumeLimit < MIN_CONSUME_LIMIT) {
            setConsumeLimit(Integer.valueOf(environment.getProperty(EventSourceConfigConst.CONSUME_LIMIT, String.valueOf(DEFAULT_CONSUME_LIMIT))));
        }
        eventSerializer = eventSerializer == null ? JSONEventSerializer.getInstance() : eventSerializer;
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
        Event event = Event.EventBuilder.newInstance()
                .name(eventName)
                .message(message)
                .sourceTerminal(TerminalFactory.create())
                .build();
        try {
            save(event);
        } catch (Exception e) {
            throw new EventbusException("EventSource push error!", e);
        }
    }
    abstract protected void save(Event event) throws Exception;
}
