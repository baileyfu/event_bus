package io.github.eventbus;

import io.github.eventbus.core.EventBusListener;
import io.github.eventbus.core.sources.filter.SubFilter;
import io.github.eventbus.core.sources.impl.DatabaseEventSource;
import io.github.eventbus.core.sources.impl.SpringEventSource;
import io.github.eventbus.core.sources.route.PubRouter;
import io.github.eventbus.core.terminal.Terminal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-01 13:52
 * @description
 */
@Configuration
public class MyConfiguration {
    @Bean
    public SpringEventSource springEventSource(){
        return new SpringEventSource("MemEventSource");
    }
    @Bean
    public DatabaseEventSource databaseEventSource(){
        return new DatabaseEventSource("DbEventSource");
    }
    @Bean
    public EventBusListener.EventHandler accountAddHandler(){
        return new EventBusListener.EventHandler(){
            @Override
            public String targetEventName() {
                return "account.add";
            }
            @Override
            public void handle(Terminal sourceTerminal, String eventName, Object message) {
                //do something for account.add
                System.out.println(eventName+" be handled ,  message : " + message+", from :"+sourceTerminal);
            }
        };
    }
    @Bean
    public EventBusListener.UniqueEventHandler allEventHandler(){
        return (sourceTerminal, eventName, message) -> {
            System.out.println(eventName+" be handled by UniqueEventHandler ,  message : " + message);
            System.out.println(sourceTerminal);
        };
    }
    @Bean
    public PubRouter learningPubRouter(){
        return new BasePubRouter() {
            private List<String> eventToRemote = Arrays.asList("learning.articleCreated");
            private List<String> eventToALl = Arrays.asList("learning.articleRead");
            @Override
            public String[] route(String eventName) {
                return eventToALl.contains(eventName) ? all :
                        eventToRemote.contains(eventName) ? global : null;
            }
        };
    }
    @Bean
    public PubRouter userAndAccountPubRouter(){
        return new BasePubRouter() {
            private List<String> eventToRemote = Arrays.asList("user.commit","account.add");
            private List<String> eventToALl = Arrays.asList("user.login");
            @Override
            public String[] route(String eventName) {
                return eventToALl.contains(eventName) ? all :
                        eventToRemote.contains(eventName) ? global : null;
            }
        };
    }
    public SubFilter subFilter(){
        return new SubFilter(){
            private List<String> acceptedEvent = Arrays.asList("some.action", "account.add");
            @Override
            public boolean doFilter(String eventName) {
                return acceptedEvent.contains(eventName);
            }
        };
    }
}
