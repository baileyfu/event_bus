package io.github.eventbus.examples;

import io.github.eventbus.annotation.EnableEventbus;
import io.github.eventbus.core.sources.filter.SubFilter;
import io.github.eventbus.core.sources.impl.SpringEventSource;
import io.github.eventbus.core.sources.impl.database.DatabaseQueueEventSource;
import io.github.eventbus.core.sources.impl.database.DatabaseTopicEventClusterSource;
import io.github.eventbus.core.sources.impl.database.DatabaseTopicEventSource;
import io.github.eventbus.core.sources.impl.database.dao.mybatis.QueuedEventWithDumpAnnotationMapper;
import io.github.eventbus.core.sources.impl.database.dao.mybatis.TopicalEventTerminalAnnotationMapper;
import io.github.eventbus.core.sources.impl.database.dao.mybatis.TopicalEventWithDumpAnnotationMapper;
import io.github.eventbus.core.sources.route.PubRouter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 使用Eventbus作为EDA的实现框架
 * @author ALi
 * @version 1.0
 * @date 2022-09-06 09:16
 * @description
 */
@EnableEventbus
@Configuration
public class EDAConfiguration {
    static String EVENTBUS_SOURCE_LOCAL = "LocalEventSource";
    static String EVENTBUS_SOURCE_DURABLE_QUEUE = "DurableQueueEventSource";
    static String EVENTBUS_SOURCE_DURABLE_TOPIC = "DurableTopicEventSource";
    static String EVENTBUS_SOURCE_DURABLE_TOPIC_CLUSTER = "DurableTopicEventClusterSource";
    //内存事件源-进程内使用
    @Bean
    public SpringEventSource eventbusSpringEventSource(){
        return new SpringEventSource(EVENTBUS_SOURCE_LOCAL);
    }
    //可持久化事件源-Topic型(非Cluster，集群中只有一个节点能消费事件)
    //@Bean
    public DatabaseQueueEventSource eventbusDatabaseQueueEventSource(QueuedEventWithDumpAnnotationMapper queuedEventWithDumpAnnotationMapper) {
        return new DatabaseQueueEventSource(EVENTBUS_SOURCE_DURABLE_QUEUE, queuedEventWithDumpAnnotationMapper);
    }
    //@Bean
    public DatabaseTopicEventSource eventbusDatabaseTopicEventSource(TopicalEventWithDumpAnnotationMapper topicalEventWithDumpAnnotationMapper, TopicalEventTerminalAnnotationMapper topicalEventTerminalAnnotationMapper) {
        return new DatabaseTopicEventSource(EVENTBUS_SOURCE_DURABLE_TOPIC, topicalEventWithDumpAnnotationMapper, topicalEventTerminalAnnotationMapper);
    }
    //可持久化事件源-Topic型(Cluster，集群中每一个节点都能消费事件)
    //@Bean
    public DatabaseTopicEventClusterSource eventbusDatabaseTopicEventClusterSource(TopicalEventWithDumpAnnotationMapper topicalEventWithDumpAnnotationMapper, TopicalEventTerminalAnnotationMapper topicalEventTerminalAnnotationMapper) {
        return new DatabaseTopicEventClusterSource(EVENTBUS_SOURCE_DURABLE_TOPIC_CLUSTER, topicalEventWithDumpAnnotationMapper, topicalEventTerminalAnnotationMapper);
    }
    //发送的事件路由---非必配，不配置则所有事件发送到所有的事件源
    //可配置多个，优先级高的先匹配；默认最低优先级
    @Bean
    public PubRouter eventbusPubRouter() {
        //discussion发送给所有durable，不发送到本地
        //learning只发送到本地
        //user发送既发送给所有durable，也发送到本地
        //事件不发送则返回null
        return eventName -> eventName.startsWith("discussion.") ? durableEventSource
                            : eventName.startsWith("learning.") ? localEventSource
                                : eventName.startsWith("user.") ? all
                                                                          : null;
    }
    //优先级更高的路由器，user事件会被发送到localEventSource
    @Bean
    public PubRouter highPriorityEventbusPubRouter() {
        return new PubRouter(){
            @Override
            public String[] route(String eventName) {
                //没有匹配到条件则返回null，让下一个路由器继续工作
                return eventName.startsWith("user.") ? localEventSource : null;
            }
            @Override
            public int getPriority() {
                return PubRouter.super.getPriority() + 1;
            }
        };
    }
    String[] localEventSource = new String[]{EVENTBUS_SOURCE_LOCAL};
    String[] durableEventSource = new String[]{EVENTBUS_SOURCE_DURABLE_TOPIC, EVENTBUS_SOURCE_DURABLE_TOPIC_CLUSTER};
    String[] all = new String[]{EVENTBUS_SOURCE_LOCAL, EVENTBUS_SOURCE_DURABLE_QUEUE, EVENTBUS_SOURCE_DURABLE_TOPIC, EVENTBUS_SOURCE_DURABLE_TOPIC_CLUSTER};
    //接收的事件过滤---非必配，不配置则接收所有的事件,但是否消费取决于是否分发对事件的处理
    @Bean
    public SubFilter eventbusSubFilter(){
        return eventName -> {
            //过滤用户登陆事件
            return !eventName.equals("user.login");
        };
    }
    @Bean
    public EDAEventDistributor edaEventDistributor(){
        return new EDAEventDistributor();
    }
}
