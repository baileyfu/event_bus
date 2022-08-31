package io.github.eventbus.terminal;

import io.github.eventbus.constants.TerminalConfigConsts;
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
    private static Terminal INSTANCE;

    public void setEnvironment(Environment environment) {
        if (INSTANCE == null) {
            INSTANCE = new Terminal();
            String name = environment.getProperty(TerminalConfigConsts.NAME);
            String ip = environment.getProperty(TerminalConfigConsts.IP);
            String port = environment.getProperty(TerminalConfigConsts.PORT);
            try{
                URL url = new URL("http", ip == null ? InetAddress.getLocalHost().getHostAddress():ip, NumberUtils.toInt(port,0), "");
                INSTANCE.setUrl(url);
                INSTANCE.setName(name == null ? url.getHost() : name);
            }catch(Exception e){
                logger.error(String.format("create Terminal error with 'name'=%s,'ip'=%s,'port'=%s",name,ip,port),e);
            }
        }
    }

    public static Terminal create(){
        return INSTANCE;
    }
}
