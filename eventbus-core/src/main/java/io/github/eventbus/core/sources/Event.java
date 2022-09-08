package io.github.eventbus.core.sources;

import io.github.eventbus.core.terminal.Terminal;
import io.github.eventbus.exception.EventbusException;
import io.github.eventbus.util.IDGenerator;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-08-31 17:00
 * @description
 */
public class Event implements Serializable {
    private String serialId;
    private String name;
    private Object message;
    private Class messageType;
    private Terminal sourceTerminal;
    private Event(){}

    public String getSerialId() {
        return serialId;
    }
    public String getName() {
        return name;
    }

    public <T> T getMessage() {
        return (T) message;
    }

    public Class getMessageType() {
        return messageType;
    }

    public Terminal getSourceTerminal() {
        return sourceTerminal;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("serialId",serialId)
                .append("name", name)
                .append("message", message)
                .append("messageType", messageType)
                .append("sourceTerminal", sourceTerminal)
                .toString();
    }

    /**
     * 事件序列化
     */
    public interface EventSerializer<T>{
        T serialize(Event event) throws EventbusException;

        Event deserialize(T serialized) throws EventbusException;
    }
    public static class EventBuilder{
        private Event event;
        private EventBuilder(){
            event = new Event();
        }
        public Event build(){
            return build(null);
        }
        public Event build(String serialId){
            event.serialId = serialId == null ? IDGenerator.GenerateEventSerialId() : serialId;
            Assert.hasLength(event.name,"eventName can not be empty!");
            return event;
        }
        public EventBuilder name(String name){
            event.name = name;
            return this;
        }
        public EventBuilder sourceTerminal(Terminal sourceTerminal){
            event.sourceTerminal = sourceTerminal;
            return this;
        }
        public EventBuilder message(Object message){
            event.message = message;
            event.messageType = message == null ? null : message.getClass();
            return this;
        }
        public static EventBuilder newInstance(){
            return new EventBuilder();
        }
    }
}
