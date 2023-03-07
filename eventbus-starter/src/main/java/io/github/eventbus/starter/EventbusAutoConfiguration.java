package io.github.eventbus.starter;

import io.github.eventbus.core.EventbusConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-10-08 10:55
 * @description
 */
@ConditionalOnMissingBean(EventbusConfiguration.class)
@Configuration
@Import(EventbusConfiguration.class)
public class EventbusAutoConfiguration {
}
