package io.github.eventbus.core.terminal;

import io.github.eventbus.constants.TerminalConfigConst;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.Optional;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-06-01 17:00
 * @description
 */
public class TerminalFactory implements EnvironmentAware {
    protected static Terminal CURRENT_TERMINAL;

    public void setEnvironment(Environment environment) {
        if (CURRENT_TERMINAL != null) {
            return;
        }
        try {
            String port = Optional.ofNullable(environment.getProperty(TerminalConfigConst.PORT))
                                  .or(() -> Optional.ofNullable(environment.getProperty("server.port")))
                                  .orElseGet(() -> environment.getProperty("local.server.port"));
            CURRENT_TERMINAL = TerminalBuilder.newInstance()
                                              .name(environment.getProperty(TerminalConfigConst.NAME))
                                              .ip(environment.getProperty(TerminalConfigConst.IP))
                                              .port(Integer.parseInt(port))
                                              .build();
        } catch (Exception e) {
            throw new RuntimeException("TerminalFactory initialization failed!", e);
        }
    }

    public static Terminal create() {
        return CURRENT_TERMINAL;
    }
}
