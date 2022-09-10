package io.github.eventbus;

import com.alibaba.fastjson.JSON;
import io.github.eventbus.annotation.EnableEventbus;
import io.github.eventbus.core.EventBusBroadcaster;
import io.github.eventbus.core.terminal.Terminal;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;
import java.util.Date;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-02 11:12
 * @description
 */
//@RunWith(SpringRunner.class)
//@SpringBootTest
@SpringBootApplication
@EnableEventbus
@RestController
public class EventBusTest {
    @RequestMapping("/{eventName}")
    public String index(@PathVariable String eventName) {
        return "broadcast : " + eventName+" , result : "+EventBusBroadcaster.broadcast(eventName,new Date());
    }
    public static void main(String[] args) throws Exception {
        //SpringApplication.run(EventBusTest.class, args);
        Terminal t=new Terminal();
        t.setName("abc");
        t.setUrl(new URL("http", "localhost", 8080, "/abc"));
        System.out.println(JSON.toJSONString(t));
    }
}
