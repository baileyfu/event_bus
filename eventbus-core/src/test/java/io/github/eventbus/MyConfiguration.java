package io.github.eventbus;

import io.github.eventbus.core.EventBusListener;
import io.github.eventbus.core.sources.filter.SubFilter;
import io.github.eventbus.core.sources.impl.database.DatabaseQueueEventSource;
import io.github.eventbus.core.sources.impl.SpringEventSource;
import io.github.eventbus.core.sources.impl.database.dao.mybatis.QueuedEventAnnotationMapper;
import io.github.eventbus.core.sources.impl.database.model.QueuedEvent;
import io.github.eventbus.core.sources.route.PubRouter;
import io.github.eventbus.core.terminal.Terminal;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
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
    public DatabaseQueueEventSource databaseEventSource(){
        return new DatabaseQueueEventSource("DbEventSource",new QueuedEventAnnotationMapper(){
            List<QueuedEvent> list = new ArrayList<>();
            @Override
            public int insert(QueuedEvent queuedEvent) {
                queuedEvent.setId(RandomUtils.nextLong());
                list.add(queuedEvent);
                return 1;
            }
            @Override
            public List<QueuedEvent> selectUnconsumedThenUpdateConsumed(String eventNames, int limit,String targetTerminal) {
                List<QueuedEvent> list = this.list;
                this.list = new ArrayList<>();
                return list;
            }
            @Override
            public int updateStateToUnconsumed(long id) {
                System.out.println("try reset state to unconsumed with id :" + id);
                return 1;
            }
            @Override
            public int cleanConsumed(String eventNames, int cycleHours) {
                return 0;
            }
        });
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
            System.out.println(eventName+" be handled by UniqueEventHandler ,  message : " + message+" , sourceTerminal : "+sourceTerminal);
        };
    }
    @Bean
    public PubRouter learningPubRouter(){
        return new BasePubRouter() {
            private List<String> eventToRemote = Arrays.asList("learning.articleCreated");
            private List<String> eventToALl = Arrays.asList("learning.articleRead","some.action");
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
    @Bean
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
