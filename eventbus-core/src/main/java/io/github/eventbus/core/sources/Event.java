package io.github.eventbus.core.sources;


import io.github.eventbus.core.terminal.Terminal;
import org.springframework.util.Assert;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-08-31 17:00
 * @description
 */
public class Event {
    private String name;
    private Object message;
    private Terminal sourceTerminal;
    private Event(){}
    public String getName() {
        return name;
    }

    public Object getMessage() {
        return message;
    }

    public Terminal getSourceTerminal() {
        return sourceTerminal;
    }
    public static class EventBuilder{
        private Event event;
        private EventBuilder(){
            event = new Event();
        }
        public Event build(){
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
            return this;
        }
        public static EventBuilder newInstance(){
            return new EventBuilder();
        }
    }
}
