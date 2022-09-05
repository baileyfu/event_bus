package io.github.eventbus.core.sources.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-05 17:21
 * @description
 */
public class PubRouterChain implements PubRouter{
    private Logger logger = LoggerFactory.getLogger(PubRouterChain.class);
    private List<PubRouter> routers;
    public PubRouterChain(List<PubRouter> routers){
        if (routers == null || routers.size() == 0) {
            logger.warn("the EBPub has no routers , use DefaultRouter instead!");
            routers = new ArrayList<>();
            routers.add(new DefaultPubRouter());
        } else if (routers.size() > 1) {
            routers.sort((r1, r2) -> r1.getPriority() < r2.getPriority() ? 1
                    : r1.getPriority() > r2.getPriority() ? -1 : 0);
        }
        this.routers = routers;
    }
    @Override
    public String[] route(String eventName) {
        String[] targetSourceNameArray = null;
        for (PubRouter router : routers) {
            targetSourceNameArray = router.route(eventName);
            if (targetSourceNameArray != null && targetSourceNameArray.length > 0) {
                return targetSourceNameArray;
            }
        }
        return null;
    }
}
