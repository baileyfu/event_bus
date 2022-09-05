package io.github.eventbus;

import io.github.eventbus.core.EventBusBroadcaster;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-02 11:12
 * @description
 */
public class EventBusBroadcasterTest {
    public void testBroadcast(){
        //do something
        EventBusBroadcaster.broadcast("something.done");
    }
}
