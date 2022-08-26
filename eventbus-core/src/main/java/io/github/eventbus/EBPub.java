package io.github.eventsbus;

import io.github.eventsbus.exception.EventsbusException;
import io.github.eventsbus.sources.EventSource;
import org.springframework.util.Assert;

import java.util.function.Supplier;

/**
 * 事件总线发布器
 *
 * @author ALi
 * @version 1.0
 * @date 2022-05-26 13:56
 * @description
 */
public class EBPub extends BusTemplate{
    public EBPub(String busName, EventSource source){
        super(busName,source);
    }

    public void emit(String eventName) throws EventsbusException {
        emit(eventName,null);
    };
    private Supplier<String> sourceMessageSupplier=()->"the EBPub "+busName+"'s source is null!";
    public void emit(String eventName, Object message) throws EventsbusException {
        Assert.notNull(source, sourceMessageSupplier);
        Assert.hasLength(eventName,"eventName can not be empty!");
        source.push(eventName,message);
    }
}
