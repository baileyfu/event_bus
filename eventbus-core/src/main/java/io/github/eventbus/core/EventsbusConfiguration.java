package io.github.eventbus.core;

import io.github.eventbus.constants.EventbusConfigConst;
import io.github.eventbus.core.sources.EventSource;
import io.github.eventbus.core.sources.route.PubRouter;
import io.github.eventbus.core.terminal.TerminalFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.Map;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-06-01 15:11
 * @description
 */
@Configuration
public class EventsbusConfiguration {
    @Bean
    public BusBroadcaster busBroadcaster(Environment environment,EBPub ebpub){
        boolean eventbusOpening = environment.getProperty(EventbusConfigConst.OPEN,Boolean.class,false);
        return new BusBroadcaster(ebpub, eventbusOpening);
    }
    @Bean
    public TerminalFactory terminalFactory(){
        return new TerminalFactory();
    }
    @Bean
    public EBPub ebpub(ApplicationContext applicationContext){
        Map<String,EventSource> esMap = applicationContext.getBeansOfType(EventSource.class);
        Map<String,PubRouter> prMap = applicationContext.getBeansOfType(PubRouter.class);
        return new EBPub(esMap == null ? null : (List) esMap.values(), prMap == null ? null : (List) prMap.values());
    }
    @Bean
    public EBSub ebsub(){
        return null;
    }
}
