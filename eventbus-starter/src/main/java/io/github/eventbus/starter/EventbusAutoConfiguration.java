package io.github.eventbus.starter;

import io.github.eventbus.annotation.EnableEventbus;
import io.github.eventbus.core.EventbusConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-10-08 10:55
 * @description
 */
@ConditionalOnMissingBean(EventbusConfiguration.class)
@Configuration
@EnableEventbus
public class EventbusAutoConfiguration {
}
