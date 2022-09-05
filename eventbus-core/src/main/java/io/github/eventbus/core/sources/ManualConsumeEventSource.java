package io.github.eventbus.core.sources;

import io.github.eventbus.constants.EventSourceConfigConst;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * 手动消费,由EBSub负责拉取
 * @author ALi
 * @version 1.0
 * @date 2022-09-05 09:36
 * @description
 */
public abstract class ManualConsumeEventSource extends AbstractEventSource implements InitializingBean {
    private long consumeInterval;
    private long pauseIfNotConsumed;
    public ManualConsumeEventSource(String name) {
        super(name);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (consumeInterval == 0l) {
            setConsumeInterval(Long.valueOf(environment.getProperty(EventSourceConfigConst.CONSUME_INTERVAL, "100")));
        }
        if (pauseIfNotConsumed == 0l) {
            setPauseIfNotConsumed(Long.valueOf(environment.getProperty(EventSourceConfigConst.PAUSE_IF_NOT_CONSUMED, "1000")));
        }
    }

    public void setConsumeInterval(long consumeInterval) {
        Assert.isTrue(consumeInterval >= 10l, "the consumeInterval can not less than 10 milliseconds.");
        this.consumeInterval = consumeInterval;
    }
    public long getConsumeInterval(){
        return consumeInterval;
    }

    public long gePauseIfNotConsumed(){
        return pauseIfNotConsumed;
    }
    public void setPauseIfNotConsumed(long pauseIfNotConsumed) {
        Assert.isTrue(pauseIfNotConsumed >= 100l, "the pauseIfNotConsumed can not less than 100 milliseconds.");
        this.pauseIfNotConsumed = pauseIfNotConsumed;
    }
}
