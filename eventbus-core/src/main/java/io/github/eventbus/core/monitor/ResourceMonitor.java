package io.github.eventbus.core.monitor;

import io.github.ali.commons.variable.MixedActionGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 资源管理器<br/>
 * 框架启动时申请的各种资源由该类负责启动/释放
 * @author ALi
 * @version 1.0
 * @date 2022-09-16 10:55
 * @description
 */
public abstract class ResourceMonitor implements Ordered {
    private static Logger LOGGER = LoggerFactory.getLogger(ResourceMonitor.class);
    private static boolean STARTED;

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    protected synchronized void doStart() {
        if (!STARTED) {
            String logHead = "Eventbus." + this.getClass().getSimpleName();
            SWITCHES.stream().forEach((Switch) -> {
                Switch.on();
                LOGGER.info(logHead + " has started action of [" + Switch.identify() + "]");
            });
            LOGGER.info(logHead + " has already started all resource!!!");
            STARTED = true;
        }
    }
    protected synchronized void doStop() {
        if (STARTED) {
            String logHead = "Eventbus." + this.getClass().getSimpleName();
            SWITCHES.stream().forEach((Switch) -> {
                try {
                    Switch.off();
                    LOGGER.info(logHead + " has stopped action of [" + Switch.identify() + "]");
                } catch (Exception e) {
                    LOGGER.error(logHead + ".doStop() error!", e);
                }
            });
            MixedActionGenerator.shutdown();
            LOGGER.info(logHead + " has already stopped all resource!!!");
            STARTED = false;
        }
    }

    /**
     * 由Monitor统一启停
     * @param Switch
     */
    public static void registerResource(Switch Switch) {
        if (!SWITCHES.contains(Switch)) {
            SWITCHES.add(Switch);
        }
    }
    private static List<Switch> SWITCHES = new ArrayList<>();
    public static abstract class Switch {
        private void on() {
            try {
                doOn();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        private void off() {
            try {
                doOff();
            } catch (Exception e) {
                LOGGER.error("ResourceMonitor.Switch." + identify() + ".off() error!", e);
            }
        }
        protected abstract void doOn() throws Exception;
        protected abstract void doOff() throws Exception;
        protected abstract String identify();

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof Switch)) {
                return false;
            }
            return Objects.equals(identify(), ((Switch) obj).identify());
        }
    }
}
