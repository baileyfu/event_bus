package io.github.eventbus.core;

import io.github.eventbus.constants.EventbusConfigConst;
import io.github.eventbus.core.monitor.ResourceMonitor;
import io.github.eventbus.core.sources.EventSource;
import io.github.eventbus.core.sources.filter.SubFilter;
import io.github.eventbus.core.sources.filter.SubFilterChain;
import io.github.eventbus.core.sources.route.PubRouter;
import io.github.eventbus.core.sources.route.PubRouterChain;
import io.github.eventbus.core.terminal.TerminalFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Map;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-06-01 15:11
 * @description
 */
@Configuration
public class EventbusConfiguration {
    @Bean
    public TerminalFactory terminalFactory(){
        return new TerminalFactory();
    }
    @Bean
    public EBPub ebpub(ApplicationContext applicationContext){
        Map<String,EventSource> esMap = applicationContext.getBeansOfType(EventSource.class);
        Map<String,PubRouter> prMap = applicationContext.getBeansOfType(PubRouter.class);
        PubRouterChain pubRouterChain = new PubRouterChain(prMap == null ? null : prMap.values());
        return new EBPub(esMap == null ? null : esMap.values(), pubRouterChain);
    }
    @Bean
    public EBSub ebsub(ApplicationContext applicationContext){
        Map<String, EventSource> esMap = applicationContext.getBeansOfType(EventSource.class);
        Map<String, SubFilter> sfMap = applicationContext.getBeansOfType(SubFilter.class);
        SubFilterChain subFilterChain = new SubFilterChain(sfMap == null ? null : sfMap.values());
        return new EBSub(esMap == null ? null : esMap.values(), subFilterChain);
    }
    @Bean
    public EventBusBroadcaster eventBusBroadcaster(Environment environment, EBPub ebpub){
        boolean open = environment.getProperty(EventbusConfigConst.OPEN,Boolean.class,false);
        boolean bctOpen = environment.getProperty(EventbusConfigConst.BCT_OPEN,Boolean.class,false);
        return new EventBusBroadcaster(ebpub, open && bctOpen);
    }
    @Bean
    public EventBusListener eventBusListener(Environment environment, EBSub ebsub, ApplicationContext applicationContext){
        Map<String, EventBusListener.EventHandler> handlerMap = applicationContext.getBeansOfType(EventBusListener.EventHandler.class);
        boolean open = environment.getProperty(EventbusConfigConst.OPEN,Boolean.class,false);
        boolean lsnOpen = environment.getProperty(EventbusConfigConst.LSN_OPEN,Boolean.class,false);
        return new EventBusListener(ebsub, handlerMap == null ? null : handlerMap.values(), open && lsnOpen);
    }
    @Bean
    public ResourceMonitor resourceMonitor(EventBusListener eventBusListener){
        return new ResourceMonitor(eventBusListener);
    }
}
