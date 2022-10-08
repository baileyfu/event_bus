package io.github.eventbus.core.sources.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
    public PubRouterChain(Collection<PubRouter> routers) {
        updateRouters(routers);
    }
    @Override
    public String[] route(String eventName) {
        String[] targetSourceNameArray;
        for (PubRouter router : routers) {
            targetSourceNameArray = router.route(eventName);
            if (targetSourceNameArray != null && targetSourceNameArray.length > 0) {
                return targetSourceNameArray;
            }
        }
        return null;
    }

    public void addRouter(PubRouter router) {
        Assert.notNull(router, "PubRouter can not be null.");
        List<PubRouter> routers = new ArrayList<>(this.routers);
        routers.add(router);
        this.routers = sorted(routers);
    }

    public void updateRouters(Collection<PubRouter> routers) {
        List<PubRouter> tempRouters;
        if (routers == null || routers.size() == 0) {
            logger.warn("the EBPub has no routers , use DefaultRouter instead!");
            tempRouters = new ArrayList<>();
            tempRouters.add(new DefaultPubRouter());
        } else {
            tempRouters = sorted(routers);
        }
        this.routers = tempRouters;
    }
    private List<PubRouter> sorted(Collection<PubRouter> routers){
        return routers.stream().sorted((r1, r2) -> r1.getPriority() < r2.getPriority() ? 1
                : r1.getPriority() > r2.getPriority() ? -1 : 0).collect(Collectors.toList());
    }
}
