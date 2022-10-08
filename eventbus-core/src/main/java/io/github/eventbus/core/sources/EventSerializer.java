package io.github.eventbus.core.sources;

import io.github.eventbus.exception.EventbusException;

/**
 * 事件序列化
 * @author ALi
 * @version 1.0
 * @date 2022-09-30 16:21
 * @description
 */
public interface EventSerializer<T>{
    T serialize(Event event) throws EventbusException;

    Event deserialize(T serialized) throws EventbusException;
}