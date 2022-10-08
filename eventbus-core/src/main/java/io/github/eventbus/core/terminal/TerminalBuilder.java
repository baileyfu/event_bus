package io.github.eventbus.core.terminal;

import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.URL;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-29 17:26
 * @description
 */
public class TerminalBuilder {
    private String name;
    private String ip;
    private int port;
    private Terminal terminal;
    private TerminalBuilder(){
    }
    TerminalBuilder name(String name) {
        this.name = name;
        return this;
    }
    TerminalBuilder ip(String ip) {
        this.ip = ip;
        return this;
    }
    TerminalBuilder port(int port) {
        this.port = port;
        return this;
    }
    Terminal build() throws Exception {
        terminal = new Terminal();
        URL url = new URL("http", StringUtils.isEmpty(ip) ? InetAddress.getLocalHost().getHostAddress() : ip, port, "");
        terminal.setUrl(url);
        terminal.setName(StringUtils.isEmpty(name) ? url.toString() : name);
        return terminal;
    }
    static TerminalBuilder newInstance() {
        return new TerminalBuilder();
    }
}
