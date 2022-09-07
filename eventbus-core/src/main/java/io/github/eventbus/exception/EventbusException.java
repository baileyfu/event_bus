package io.github.eventbus.exception;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-05-27 18:27
 * @description
 */
public class EventbusException extends Exception{
    private static final String HEAD = "[Eventbus]-";
    public EventbusException(){
        super(HEAD + "eventbus appear error !");
    }
    public EventbusException(String message){
        super(HEAD+message);
    }
    public EventbusException(String message, Throwable cause){
        super(HEAD+message,cause);
    }
}
