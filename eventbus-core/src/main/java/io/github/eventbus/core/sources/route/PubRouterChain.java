package io.github.eventbus.core.sources.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-05 17:21
 * @description
 */
public class PubRouterChain implements PubRouter{
    private Logger logger = LoggerFactory.getLogger(PubRouterChain.class);
    private List<PubRouter> routers;
    public PubRouterChain(Collection<PubRouter> routers){
        if (routers == null || routers.size() == 0) {
            logger.warn("the EBPub has no routers , use DefaultRouter instead!");
            this.routers = new ArrayList<>();
            this.routers.add(new DefaultPubRouter());
        } else {
            this.routers = routers.stream().sorted((r1, r2) -> r1.getPriority() < r2.getPriority() ? 1
                    : r1.getPriority() > r2.getPriority() ? -1 : 0).collect(Collectors.toList());
        }
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
