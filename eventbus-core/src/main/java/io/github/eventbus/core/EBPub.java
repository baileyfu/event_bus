package io.github.eventbus.core;

import io.github.eventbus.core.sources.EventSource;
import io.github.eventbus.core.sources.route.DefaultPubRouter;
import io.github.eventbus.core.sources.route.PubRouter;
import io.github.eventbus.exception.EventbusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 事件总线发布器
 *
 * @author ALi
 * @version 1.0
 * @date 2022-05-26 13:56
 * @description
 */
public class EBPub {
    private Logger logger = LoggerFactory.getLogger(EBPub.class);
    private Map<String,EventSource> sources;
    private String[] sourceKeys;
    private List<PubRouter> routers;

    public EBPub(List<EventSource> sources, List<PubRouter> routers) {
        Assert.noNullElements(sources, "the EBPub has no EventSource!");
        if (routers == null || routers.size() == 0) {
            logger.warn("the EBPub has no routers , use DefaultRouter instead!");
            routers = new ArrayList<>();
            routers.add(new DefaultPubRouter());
        } else if (routers.size() > 1) {
            routers.sort((r1, r2) -> r1.getPriority() < r2.getPriority() ? 1
                                   : r1.getPriority() > r2.getPriority() ? -1 : 0);
        }
        this.sources = sources.stream().reduce(new HashMap<String, EventSource>(), (map, es) -> {
            Assert.isTrue(!map.containsKey(es.getName()), "duplicated EventSource name : '" + es.getName() + "'.");
            map.put(es.getName(), es);
            return map;
        }, (m, n) -> m);
        this.sourceKeys = this.sources.keySet().toArray(new String[0]);
        this.routers = routers;
    }

    public void emit(String eventName) throws EventbusException {
        emit(eventName, null);
    }


    public void emit(String eventName, Object message) throws EventbusException {
        Assert.hasLength(eventName, "eventName can not be empty!");
        String[] targetSourceNameArray = null;
        for (PubRouter router : routers) {
            targetSourceNameArray = router.getClass().isAssignableFrom(DefaultPubRouter.class) ? sourceKeys : router.route(eventName);
            if ((targetSourceNameArray != null && targetSourceNameArray.length > 0)) {
                doEmit(eventName, message, targetSourceNameArray);
                break;
            }
        }
    }
    private void doEmit(String eventName, Object message,String...sourceNameArray) throws EventbusException {
        for (String sourceName : sourceNameArray) {
            EventSource eventSource = sources.get(sourceName);
            if (eventSource != null) {
                eventSource.push(eventName, message);
                continue;
            }
            logger.warn("No EventSource with the name '?' be found!", sourceName);
        }
    }
}
