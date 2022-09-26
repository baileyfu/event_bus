package io.github.eventbus.examples;

import io.github.eventbus.core.EventBusBroadcaster;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-26 15:56
 * @description
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = EDAConfiguration.class)
public class SpringApplication {
    @Test
    public void testPublish() throws Exception{
        EventBusBroadcaster.broadcast("unknown.event");
        EventBusBroadcaster.broadcast("user.login","10086");
        Map<String, String> message = new HashMap<>();
        message.put("contentId", "1");
        message.put("contentValue", "hello world");
        message.put("userId", "10086");
        if (EventBusBroadcaster.broadcast("learning.content.created", message)) {
            System.out.println("broadcast succeed!");
        }
        Thread.sleep(5_000);
    }
}
