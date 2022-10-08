package io.github.eventbus.core.sources.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-05 17:00
 * @description
 */
public class SubFilterChain implements SubFilter{
    private Logger logger = LoggerFactory.getLogger(SubFilterChain.class);
    private Collection<SubFilter> filters;
    private List<ListenedFilterChangingListener> filterChangingListeners;

    public SubFilterChain(Collection<SubFilter> filters) {
        this.filters = filters;
    }
    public void registerFilterChangingListener(ListenedFilterChangingListener listenedFilterChangingListener){
        Assert.notNull(listenedFilterChangingListener,"listenedFilterChangingListener can not be null!");
        filterChangingListeners = filterChangingListeners == null ? new ArrayList<>() : filterChangingListeners;
        filterChangingListeners.add(listenedFilterChangingListener);
    }
    @Override
    public boolean doFilter(String eventName) {
        if (filters != null && filters.size() > 0) {
            for (SubFilter filter : filters) {
                if (!filter.doFilter(eventName)) {
                    return false;
                }
            }
        }
        return true;
    }
    public void addFilter(SubFilter filter){
        Assert.notNull(filter, "SubFilter can not be null.");
        this.filters.add(filter);
        invokeFilterChangingListener();
    }
    public void updateFilters(Collection<SubFilter> filters){
        this.filters = filters;
        invokeFilterChangingListener();
    }
    private void invokeFilterChangingListener() {
        if (filterChangingListeners != null && filterChangingListeners.size() > 0) {
            for (ListenedFilterChangingListener filterChangingListener : filterChangingListeners) {
                try {
                    filterChangingListener.notifyCausedByFilterChanging();
                } catch (Exception e) {
                    logger.error("SubFilterChain.updateFilters error !", e);
                }
            }
        }
    }
    /**
     * 过滤器变更监听
     */
    public interface ListenedFilterChangingListener{
        /**
         * 过滤器规则发生变更的通知
         */
        void notifyCausedByFilterChanging();
    }
}
