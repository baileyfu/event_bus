package io.github.eventbus.constants;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-05 14:09
 * @description
 */
public class EventSourceConfigConst {
    public static final String CONSUME_LIMIT = "eventbus.source.consume.limit";

    public static final String MANUAL_CONSUME_INTERVAL = "eventbus.source.manual.consumeInterval";
    public static final String MANUAL_PAUSE_IF_NOT_CONSUMED = "eventbus.source.manual.pauseIfNotConsumed";

    public static final String MANUAL_DATABASE_CLEAN_REQUIRED = "eventbus.source.manual.database.clean.required";
    public static final String MANUAL_DATABASE_CLEAN_CYCLE = "eventbus.source.manual.database.clean.cycle";

    public static final String MANUAL_DATABASE_TOPIC_INACTIVATE_REQUIRED = "eventbus.source.manual.database.topic.inactivate.required";
    public static final String MANUAL_DATABASE_TOPIC_INACTIVATE_CYCLE = "eventbus.source.manual.database.topic.inactivate.cycle";
}
