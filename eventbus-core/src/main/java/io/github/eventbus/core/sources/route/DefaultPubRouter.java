package io.github.eventbus.core.sources.route;

/**
 * 默认路由-发送到所有事件源
 *
 * @author ALi
 * @version 1.0
 * @date 2022-09-01 09:59
 * @description
 */
public class DefaultPubRouter implements PubRouter {
    public final static String[] PUB_TO_ALL = new String[]{"ALL"};
    @Override
    public String[] route(String eventName) {
        return PUB_TO_ALL;
    }
}
