package io.github.eventbus.util;

import java.util.UUID;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-08 10:14
 * @description
 */
public class IDGenerator {
    /**
     * 事件序列号长度50
     * @return
     */
    public static String GenerateEventSerialId(){
        return new StringBuilder().append(System.currentTimeMillis()).append("-").append(UUID.randomUUID()).toString();
    }
}
