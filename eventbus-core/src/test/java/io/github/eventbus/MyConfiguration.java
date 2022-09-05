package io.github.eventbus;

import io.github.eventbus.core.EventBusListener;
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
            }
        };
    }
    @Bean
    public EventBusListener.UniqueEventHandler allEventHandler(){
        return new EventBusListener.UniqueEventHandler(){
            @Override
            public void handle(Terminal sourceTerminal, String eventName, Object message) {

            }
        };
    }
    @Bean
    public PubRouter learningPubRouter(){
        return new BasePubRouter() {
            private List<String> globalEvent = Arrays.asList("learning.articleCreated");
            private List<String> allEvent = Arrays.asList("learning.articleRead");
            @Override
            public String[] route(String eventName) {
                return allEvent.contains(eventName) ? all :
                        globalEvent.contains(eventName) ? global : local;
            }
        };
    }
    @Bean
    public PubRouter userPubRouter(){
        return new BasePubRouter() {
            private List<String> globalEvent = Arrays.asList("user.commit");
            private List<String> allEvent = Arrays.asList("user.login");
            @Override
            public String[] route(String eventName) {
                return allEvent.contains(eventName) ? all :
                        globalEvent.contains(eventName) ? global : local;
            }
        };
    }
}
