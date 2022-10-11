package io.github.eventbus.core.sources.impl.database;

import com.alibaba.fastjson.JSON;
import io.github.ali.commons.variable.MixedActionGenerator;
import io.github.eventbus.constants.EventSourceConfigConst;
import io.github.eventbus.constants.JSONConfig;
import io.github.eventbus.core.monitor.ResourceMonitor;
import io.github.eventbus.core.sources.ManualConsumeEventSource;
import io.github.eventbus.core.terminal.Terminal;
import io.github.eventbus.core.terminal.TerminalFactory;
import io.github.eventbus.exception.EventbusException;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 数据库事件源基类<br/>
 * 负责清理已消费事件
 *
 * @author ALi
 * @version 1.0
 * @date 2022-09-07 17:42
 * @description
 */
public abstract class AbstractDatabaseEventSource extends ManualConsumeEventSource {
    public static final int CLEAN_ACTION_INTERVAL_MINUTES = 30;

    public static final boolean DEFAULT_CLEAN_REQUIRED = true;

    public static final int DEFAULT_CLEAN_CYCLE = 1;
    public static final int MIN_CLEAN_CYCLE = 1;

    //是否需要清理已消费事件
    protected Boolean cleaningRequired;
    //清理已消费事件的间隔（单位：小时）
    protected int cleanCycle;
    protected String serializedTerminalForConsumed;

    public AbstractDatabaseEventSource(String name) {
        super(name);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.serializedTerminalForConsumed = serializeTerminal(TerminalFactory.create());
        if (cleaningRequired == null) {
            setCleaningRequired(Boolean.valueOf(environment.getProperty(EventSourceConfigConst.MANUAL_DATABASE_CLEAN_REQUIRED, String.valueOf(DEFAULT_CLEAN_REQUIRED))));
        }
        if (cleanCycle < MIN_CLEAN_CYCLE) {
            setCleanCycle(Integer.valueOf(environment.getProperty(EventSourceConfigConst.MANUAL_DATABASE_CLEAN_CYCLE, String.valueOf(DEFAULT_CLEAN_CYCLE))));
        }
        if (cleaningRequired) {
            //启动定时(30分钟)清理已消费事件
            final String actionName = this + ".clean";
            ResourceMonitor.registerResource(new ResourceMonitor.Switch() {
                @Override
                public void doOn() throws Exception {
                    MixedActionGenerator.loadAction(actionName, CLEAN_ACTION_INTERVAL_MINUTES, TimeUnit.MINUTES, () -> {
                        try {
                            clean();
                        } catch (Exception e) {
                            logger.error("the action of " + actionName + ".clean() error!", e);
                        }
                    });
                }
                @Override
                public void doOff() throws Exception {
                    MixedActionGenerator.unloadAction(actionName, false);
                }
                @Override
                public String identify() {
                    return actionName;
                }
            });
        }
    }

    public void setCleanCycle(int cleanCycle) {
        this.cleanCycle = cleanCycle;
        if (this.cleanCycle < MIN_CLEAN_CYCLE) {
            this.cleanCycle = DEFAULT_CLEAN_CYCLE;
            logger.warn(EventSourceConfigConst.MANUAL_DATABASE_CLEAN_CYCLE + " value is " + cleanCycle + " , reset to " + DEFAULT_CLEAN_CYCLE);
        }
    }

    public void setCleaningRequired(boolean cleaningRequired) {
        this.cleaningRequired = cleaningRequired;
    }

    /**
     * 清理已消费事件
     * @throws Exception
     */
    abstract protected void clean() throws Exception;

    /*** 序列化message和terminal ***/
    protected static String serializeMessage(Object message) throws EventbusException {
        try {
            return message == null ? StringUtils.EMPTY : JSON.toJSONString(message, JSONConfig.SERIALIZER_FEATURE_ARRAY);
        } catch (Exception e) {
            throw new EventbusException("DatabaseEventSource.serializeMessage() error!", e);
        }
    }
    protected static Object deserializeMessage(String jsonMessage,String messageTypeValue) throws EventbusException{
        try {
            return StringUtils.isEmpty(jsonMessage) ? null : JSON.parseObject(jsonMessage, Class.forName(messageTypeValue), JSONConfig.FEATURE_ARRAY);
        } catch (Exception e) {
            throw new EventbusException("DatabaseEventSource.deserializeMessage() error!", e);
        }
    }
    protected static String serializeTerminal(Terminal sourceTerminal) throws EventbusException {
        try {
            return sourceTerminal == null ? StringUtils.EMPTY : JSON.toJSONString(sourceTerminal);
        } catch (Exception e) {
            throw new EventbusException("DatabaseEventSource.serializeTerminal() error!", e);
        }
    }
    protected static Terminal deserializeTerminal(String jsonSourceTerminal) throws EventbusException {
        try {
            return StringUtils.isEmpty(jsonSourceTerminal) ? null : JSON.parseObject(jsonSourceTerminal, Terminal.class);
        } catch (Exception e) {
            throw new EventbusException("DatabaseEventSource.deserializeTerminal() error!", e);
        }
    }
}
