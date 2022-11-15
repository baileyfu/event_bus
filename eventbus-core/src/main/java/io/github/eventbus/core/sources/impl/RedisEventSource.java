package io.github.eventbus.core.sources.impl;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import io.github.eventbus.core.EBSub;
import io.github.eventbus.core.event.Event;
import io.github.eventbus.core.event.EventSerializer;
import io.github.eventbus.core.sources.ManualConsumeEventSource;
import io.github.eventbus.exception.EventbusException;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * RedisTemplate若未设置ValueSerializer则默认ValueSerializer为FastJsonRedisSerializer
 * @author ALi
 * @version 1.0
 * @date 2022-10-09 10:03
 * @description
 */
public class RedisEventSource extends ManualConsumeEventSource implements EBSub.ListenedEventChangingListener{
    private List<String> listenedEvents;
    private RedisTemplate redisTemplate;
    public RedisEventSource(String name, RedisTemplate redisTemplate) {
        super(name);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        RedisSerializer valueSerializer = Optional.ofNullable(redisTemplate.getValueSerializer())
                                                  .orElseGet(()->new FastJsonRedisSerializer(Event.class));
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        //value由EventSerializer来负责序列化，redisTemplate直接操作序列化后的value
        redisTemplate.setValueSerializer(null);
        setEventSerializer(new EventSerializer() {
            @Override
            public Object serialize(Event event) throws EventbusException {
                return valueSerializer.serialize(event);
            }
            @Override
            public Event deserialize(Object serialized) throws EventbusException {
                return (Event) valueSerializer.deserialize((byte[]) serialized);
            }
        });
    }

    @Override
    protected void save(String eventName, Object serializedEvent) throws Exception {
        //尾部入队
        redisTemplate.opsForList().rightPush(eventName, serializedEvent);
    }

    /**
     * 单次消费数量区间:[0 - (2 * consumeLimit - 1)]
     * @return
     * @throws Exception
     */
    @Override
    protected List<SerializedEventWrapper> fetchAndSetConsumed() throws Exception {
        if (listenedEvents == null) {
            logger.info("RedisEventSource.fetchAndSetUnconsumed() listenedEvents is empty , no event will be fetched.");
            return null;
        }
        List<SerializedEventWrapper> unconsumedSerializedEventWrapperList = null;
        ListOperations listOperations = redisTemplate.opsForList();
        for (String listenedEvent : listenedEvents) {
            //头部出队
            List<byte[]> fetchedEvents = listOperations.range(listenedEvent, 0, consumeLimit - 1);
            if (fetchedEvents != null && fetchedEvents.size() > 0) {
                unconsumedSerializedEventWrapperList = unconsumedSerializedEventWrapperList == null ? new ArrayList<>() : unconsumedSerializedEventWrapperList;
                for (byte[] eventBytes : fetchedEvents) {
                    unconsumedSerializedEventWrapperList.add(new SerializedEventWrapper(listenedEvent, eventBytes));
                }
                if (unconsumedSerializedEventWrapperList.size() >= consumeLimit) {
                    break;
                }
            }
        }
        return unconsumedSerializedEventWrapperList;
    }

    @Override
    protected void setUnconsumed(SerializedEventWrapper serializedEventWrapper) throws Exception {
        save(serializedEventWrapper.getKey(), serializedEventWrapper.getSerializedEvent());
    }

    @Override
    public void notifyCausedByListenedEventChanging(List<String> listenedEvents) {
        this.listenedEvents = listenedEvents;
    }
}
