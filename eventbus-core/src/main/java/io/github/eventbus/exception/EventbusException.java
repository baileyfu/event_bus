package io.github.eventbus.exception;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-05-27 18:27
 * @description
 */
public class EventbusException extends Exception{
    public EventbusException(){
    }
    public EventbusException(String message){
        super(message);
    }
    public EventbusException(String message, Throwable cause){
        super(message,cause);
    }
}
