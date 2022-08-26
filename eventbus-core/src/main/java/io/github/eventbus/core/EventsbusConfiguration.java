package io.github.eventsbus.core;

import io.github.eventsbus.terminal.TerminalFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-06-01 15:11
 * @description
 */
@Configuration
public class EventsbusConfiguration {
    @Bean
    public TerminalFactory terminalFactory(){
        return new TerminalFactory();
    }
}
