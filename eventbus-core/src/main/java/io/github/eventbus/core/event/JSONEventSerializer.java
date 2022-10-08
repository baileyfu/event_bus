package io.github.eventbus.core.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.github.eventbus.constants.JSONConfig;
import io.github.eventbus.core.terminal.Terminal;
import io.github.eventbus.exception.EventbusException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-08 15:03
 * @description
 */
public class JSONEventSerializer implements EventSerializer<String> {
    private static JSONEventSerializer INSTANCE;
    private Logger logger = LoggerFactory.getLogger(JSONEventSerializer.class);

    public static JSONEventSerializer getInstance(){
        if (INSTANCE == null) {
            INSTANCE = new JSONEventSerializer();
        }
        return INSTANCE;
    }

    @Override
    public String serialize(Event event) throws EventbusException {
        try{
            return JSON.toJSONString(event, JSONConfig.SERIALIZER_FEATURE_ARRAY);
        }catch(Exception e){
            logger.error("JSONEventSerializer.serialize() with '" + event + "' error!");
            throw new EventbusException("JSONEventSerializer.serialize() error!", e);
        }
    }

    @Override
    public Event deserialize(String serialized) throws EventbusException {
        JSONObject jsonObject = JSON.parseObject(serialized);
        if (jsonObject == null) {
            return null;
        }
        Event.EventBuilder eventBuilder = Event.EventBuilder.newInstance().name(jsonObject.getString("name"));
        String messageValue = jsonObject.getString("message");
        if (StringUtils.isNotEmpty(messageValue)) {
            Class messageType = null;
            try {
                messageType = Class.forName(jsonObject.getString("messageType"));
            } catch (Exception e) {
                logger.error("JSONEventSerializer.deserialize() with '" + serialized + "' error !");
                throw new EventbusException("JSONEventSerializer.deserialize() error!", e);
            }
            eventBuilder.message(JSON.parseObject(messageValue, messageType, JSONConfig.FEATURE_ARRAY));
        }
        String sourceTerminalValue = jsonObject.getString("sourceTerminal");
        if (StringUtils.isNotEmpty(sourceTerminalValue)) {
            eventBuilder.sourceTerminal(JSON.parseObject(sourceTerminalValue, Terminal.class));
        }
        return eventBuilder.build(jsonObject.getString("serialId"));
    }
}
