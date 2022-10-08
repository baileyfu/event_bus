package io.github.eventbus.core.sources;

import io.github.ali.commons.variable.MixedActionGenerator;
import io.github.eventbus.constants.EventSourceConfigConst;
import io.github.eventbus.exception.EventbusException;

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
    protected void startConsume(Function<String, EventConsumer> consumerGetter) {
        MixedActionGenerator.loadAction(generateActionName(),consumeInterval, TimeUnit.MILLISECONDS,()->{
            try {
                // 如果没消费到消息则暂停x毫秒
                if (doConsume(consumerGetter) == 0 && pauseIfNotConsumed > 0) {
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
        MixedActionGenerator.unloadAction(generateActionName(),false);
    }
    private String generateActionName() {
        return new StringBuilder()
                .append("Eventbus.EventSource.")
                .append(getName())
                .append(".consuming")
                .toString();
    }
    abstract protected int doConsume(Function<String, EventConsumer> consumerGetter) throws EventbusException;
}
