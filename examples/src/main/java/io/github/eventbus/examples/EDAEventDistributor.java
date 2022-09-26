package io.github.eventbus.examples;

import io.github.eventbus.core.EventBusListener;
import io.github.eventbus.core.terminal.Terminal;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

/**
 * EDA事件分发器<br/>
 * 定义如何处理接收的事件
 * @author ALi
 * @version 1.0
 * @date 2022-09-16 17:36
 * @description
 */
public class EDAEventDistributor implements ApplicationListener<ApplicationStartedEvent> {

    /**
     * 处理来自User的事件
     */
    private void distributeFromUser() {
        EventBusListener.listen(new EventBusListener.EventHandler() {
            @Override
            public String targetEventName() {
                return "user.login";
            }
            @Override
            public void handle(Terminal sourceTerminal, String eventName, Object message) {
                //do something for user.login
                //if failed you can throw a exception to rollback the event consumed
            }
        });
        EventBusListener.listen("user.logout",(Terminal sourceTerminal, String eventName, Object message)->{
                //do something for user.logout
        });
    }

    /**
     * 处理来自Discussion的事件
     */
    private void distributeFromDiscussion(){
        EventBusListener.listen(new EventBusListener.EventHandler() {
            @Override
            public String targetEventName() {
                return "discussion.content.created";
            }
            @Override
            public void handle(Terminal sourceTerminal, String eventName, Object message) {
                //do something for discussion.created
            }
        });
    }

    /**
     * 处理来自Learning的事件
     */
    private void distributeFromLearning(){
        EventBusListener.listen("learning.content.created",(Terminal sourceTerminal, String eventName, Object message)->{
            //do something for learning.content.created
            System.out.println("consumed event------------>" + eventName + " , from " + sourceTerminal + " , message : " + message);
        });
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
        distributeFromUser();
        distributeFromDiscussion();
        distributeFromLearning();
    }
}
