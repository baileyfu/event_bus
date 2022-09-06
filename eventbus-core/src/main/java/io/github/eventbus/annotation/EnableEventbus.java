package io.github.eventbus.annotation;

import io.github.eventbus.core.EventbusConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-06-01 15:49
 * @description
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(EventbusConfiguration.class)
public @interface EnableEventbus {
}
