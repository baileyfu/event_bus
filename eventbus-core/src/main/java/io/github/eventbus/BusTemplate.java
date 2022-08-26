package io.github.eventsbus;

import io.github.eventsbus.sources.EventSource;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-06-01 18:30
 * @description
 */
public class BusTemplate {
    protected String busName;
    protected EventSource source;

    protected BusTemplate(String busName, EventSource source){
        this.busName = busName;
        this.source = source;
    }
    public String getBusName() {
        return busName;
    }
}
