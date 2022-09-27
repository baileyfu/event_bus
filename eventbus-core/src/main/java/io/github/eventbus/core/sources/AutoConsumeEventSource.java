package io.github.eventbus.core.sources;

/**
 * 由具体依赖的消息队列拉取
 * @author ALi
 * @version 1.0
 * @date 2022-09-05 09:38
 * @description
 */
public abstract class AutoConsumeEventSource extends AbstractEventSource{
    public AutoConsumeEventSource(String name) {
        super(name);
    }
}
