package io.github.eventbus;

import io.github.eventbus.core.sources.route.PubRouter;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-01 19:44
 * @description
 */
public abstract class BasePubRouter implements PubRouter {
    protected String[] local = new String[]{"MemEventSource"};
    protected String[] global = new String[]{"DbEventSource"};
    protected String[] all = new String[]{"MemEventSource", "DbEventSource"};
}
