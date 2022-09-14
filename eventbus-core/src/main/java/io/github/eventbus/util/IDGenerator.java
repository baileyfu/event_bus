package io.github.eventbus.util;

import io.github.eventbus.core.terminal.Terminal;
import org.apache.commons.lang3.builder.ToStringBuilder;

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
    public static String generateEventSerialId(){
        return new StringBuilder().append(System.currentTimeMillis()).append("-").append(UUID.randomUUID()).toString();
    }

    public static String generateTerminalId(Terminal terminal) {
        return new StringBuilder()
                .append(terminal.getName())
                .append("-")
                .append(terminal.getUrl())
                .toString();
    }
}
