package io.github.eventbus.core.sources.filter;

import java.util.Collection;
import java.util.List;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-05 17:00
 * @description
 */
public class SubFilterChain implements SubFilter{
    private Collection<SubFilter> filters;

    public SubFilterChain(Collection<SubFilter> filters) {
        this.filters = filters;
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
}
