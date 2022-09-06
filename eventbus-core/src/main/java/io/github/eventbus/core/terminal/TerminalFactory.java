package io.github.eventbus.core.terminal;

import io.github.eventbus.constants.TerminalConfigConst;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.URL;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-06-01 17:00
 * @description
 */
public class TerminalFactory implements EnvironmentAware {
    private static Logger logger= LoggerFactory.getLogger(TerminalFactory.class);
    private static Terminal CURRENT_TERMINAL;
    public void setEnvironment(Environment environment) {
        if (CURRENT_TERMINAL == null) {
            CURRENT_TERMINAL = new Terminal();
            String name = environment.getProperty(TerminalConfigConst.NAME);
            String ip = environment.getProperty(TerminalConfigConst.IP);
            String port = environment.getProperty(TerminalConfigConst.PORT);
            try{
                URL url = new URL("http", ip == null ? InetAddress.getLocalHost().getHostAddress():ip, NumberUtils.toInt(port,0), "");
                CURRENT_TERMINAL.setUrl(url);
                CURRENT_TERMINAL.setName(name == null ? url.getHost() : name);
            }catch(Exception e){
                logger.error(String.format("create Terminal error with 'name'=%s,'ip'=%s,'port'=%s",name,ip,port),e);
            }
        }
    }

    public static Terminal create(){
        return CURRENT_TERMINAL;
    }
}
