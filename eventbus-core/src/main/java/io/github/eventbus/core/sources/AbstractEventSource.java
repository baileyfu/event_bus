package io.github.eventbus.core.sources;

import io.github.eventbus.exception.EventbusException;

import io.github.eventbus.core.terminal.TerminalFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;


/**
 * @author ALi
 * @version 1.0
 * @date 2022-06-01 17:33
 * @description
 */
public abstract class AbstractEventSource implements EventSource, EnvironmentAware {
    protected Environment environment;
    private String name;

    public AbstractEventSource(String name) {
        Assert.hasLength(name, "the EventSource'name can not be empty!");
        this.name = name;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
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
