package io.github.eventbus;

import io.github.eventbus.core.sources.impl.DatabaseEventSource;
import io.github.eventbus.core.sources.impl.SpringEventSource;
import io.github.eventbus.core.sources.route.PubRouter;
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
    public PubRouter learningPubRouter(){
        return new BasePubRouter() {
            private List<String> globalEvent = Arrays.asList("articleCreated");
            private List<String> allEvent = Arrays.asList("articleRead");
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
            private List<String> globalEvent = Arrays.asList("userCommit");
            private List<String> allEvent = Arrays.asList("userLogin");
            @Override
            public String[] route(String eventName) {
                return allEvent.contains(eventName) ? all :
                        globalEvent.contains(eventName) ? global : local;
            }
        };
    }
}
