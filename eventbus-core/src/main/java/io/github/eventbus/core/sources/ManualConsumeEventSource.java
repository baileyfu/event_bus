package io.github.eventbus.core.sources;

import com.alibaba.fastjson.JSON;
import io.github.ali.commons.variable.MixedActionGenerator;
import io.github.eventbus.constants.EventSourceConfigConst;
import io.github.eventbus.exception.EventbusException;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 手动消费,利用MixedActionGenerator定时拉取
 * @author ALi
 * @version 1.0
 * @date 2022-09-05 09:36
 * @description
 */
public abstract class ManualConsumeEventSource extends AbstractEventSource{
    public static final long DEFAULT_CONSUME_INTERVAL = 100l;
    public static final long MIN_CONSUME_INTERVAL = 10l;

    public static final long DEFAULT_CONSUME_PAUSE = 1000l;
    public static final long MIN_CONSUME_PAUSE = 1000l;

    //消费间隔
    private long consumeInterval;
    //事件列表未空时消费线程暂停事件
    private long pauseIfNotConsumed;
    private String actionNameOfConsuming;
    public ManualConsumeEventSource(String name) {
        super(name);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        if (consumeInterval < MIN_CONSUME_INTERVAL) {
            setConsumeInterval(Long.valueOf(environment.getProperty(EventSourceConfigConst.MANUAL_CONSUME_INTERVAL, String.valueOf(DEFAULT_CONSUME_INTERVAL))));
        }
        if (pauseIfNotConsumed < MIN_CONSUME_PAUSE) {
            setPauseIfNotConsumed(Long.valueOf(environment.getProperty(EventSourceConfigConst.MANUAL_PAUSE_IF_NOT_CONSUMED, String.valueOf(DEFAULT_CONSUME_PAUSE))));
        }
        actionNameOfConsuming = new StringBuilder()
                                    .append("Eventbus.EventSource.")
                                    .append(getName())
                                    .append(".consuming")
                                    .toString();
    }

    public void setConsumeInterval(long consumeInterval) {
        this.consumeInterval = consumeInterval;
        if (this.consumeInterval < MIN_CONSUME_INTERVAL) {
            this.consumeInterval = DEFAULT_CONSUME_INTERVAL;
            logger.warn(EventSourceConfigConst.MANUAL_CONSUME_INTERVAL + " value is " + consumeInterval + " , reset to " + DEFAULT_CONSUME_INTERVAL);
        }
    }

    public void setPauseIfNotConsumed(long pauseIfNotConsumed) {
        this.pauseIfNotConsumed = pauseIfNotConsumed;
        if (this.pauseIfNotConsumed < MIN_CONSUME_PAUSE) {
            this.pauseIfNotConsumed = DEFAULT_CONSUME_PAUSE;
            logger.warn(EventSourceConfigConst.MANUAL_PAUSE_IF_NOT_CONSUMED + " value is " + pauseIfNotConsumed + " , reset to " + DEFAULT_CONSUME_PAUSE);
        }
    }

    @Override
    protected void startConsume(Function<Object, Boolean> consumer) {
        MixedActionGenerator.loadAction(actionNameOfConsuming,consumeInterval, TimeUnit.MILLISECONDS,()->{
            try {
                int consumedCount = 0;
                List<SerializedEventWrapper> unconsumedSerializedEventWrapperList = fetchAndSetConsumed();
                if (unconsumedSerializedEventWrapperList != null && unconsumedSerializedEventWrapperList.size() > 0) {
                    for (SerializedEventWrapper serializedEventWrapper : unconsumedSerializedEventWrapperList) {
                        try {
                            if (consumer.apply(serializedEventWrapper.getSerializedEvent())) {
                                consumedCount++;
                            }
                        } catch (Exception e) {
                            logger.error(this + " consume error with '" + SerializedEventWrapperToString(serializedEventWrapper) + "'!", e);
                            //单个事件消费失败不影响其他事件的消费
                            try {
                                //将事件状态重制为unconsumed
                                setUnconsumed(serializedEventWrapper);
                            } catch (Exception rollbackException) {
                                logger.error(this + " rollback error for Event '" + serializedEventWrapper + "!", rollbackException);
                                LoggerFactory.getLogger(this.getClass()).error(this + " rollback consumed failed , the event is " + SerializedEventWrapperToString(serializedEventWrapper));
                            }
                        }
                    }
                }
                // 如果没消费到消息则暂停x毫秒
                if (consumedCount == 0 && pauseIfNotConsumed > 0) {
                    Thread.sleep(pauseIfNotConsumed);
                }
            } catch (Exception e) {
                logger.error(getName() + " consume error !", e);
                // 消费出错后暂停100ms
                try {
                    Thread.sleep(100l);
                } catch (InterruptedException ie) {
                    logger.error(getName() + " sleeping after consuming failed error !", ie);
                }
            }
        });
    }

    @Override
    protected void stopConsume() {
        //由ResourceReleaser来负责最终的释放
        MixedActionGenerator.unloadAction(actionNameOfConsuming,false);
    }

    private String SerializedEventWrapperToString(SerializedEventWrapper serializedEventWrapper){
        try {
            return new ToStringBuilder("Event")
                    .append("key", serializedEventWrapper.key)
                    .append("value", JSON.toJSONString(getEventSerializer().deserialize(serializedEventWrapper.serializedEvent)))
                    .toString();
        } catch (EventbusException e) {
            logger.error("SerializedEventWrapperToString error!", e);
            return serializedEventWrapper.toString();
        }
    }
    /**
     * 查询待消费事件并将其设置为已消费
     * @return
     * @throws Exception
     */
    abstract protected List<SerializedEventWrapper> fetchAndSetConsumed() throws Exception;

    /**
     * 回滚事件的消费
     * @param serializedEventWrapper
     * @throws Exception
     */
    abstract protected void setUnconsumed(SerializedEventWrapper serializedEventWrapper) throws Exception;

    protected static class SerializedEventWrapper{
        private Object key;
        private Object serializedEvent;

        public SerializedEventWrapper(Object key, Object serializedEvent) {
            this.key = key;
            this.serializedEvent = serializedEvent;
        }

        public <T> T getKey() {
            return (T) key;
        }

        public <T> T getSerializedEvent() {
            return (T) serializedEvent;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("key", key)
                    .append("serializedEvent", serializedEvent)
                    .toString();
        }
    }
}
