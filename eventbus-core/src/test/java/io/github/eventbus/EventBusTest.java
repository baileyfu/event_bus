package io.github.eventbus;

import io.github.eventbus.annotation.EnableEventsbus;
import io.github.eventbus.core.EventBusBroadcaster;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-02 11:12
 * @description
 */
//@RunWith(SpringRunner.class)
//@SpringBootTest
@SpringBootApplication
@EnableEventsbus
@RestController
public class EventBusTest {
    @RequestMapping("/{eventName}")
    public String index(@PathVariable String eventName) {
        return "broadcast : " + eventName+" , result : "+EventBusBroadcaster.broadcast(eventName);
    }
    public static void main(String[] args) throws Exception {
        SpringApplication.run(EventBusTest.class, args);
    }
}
