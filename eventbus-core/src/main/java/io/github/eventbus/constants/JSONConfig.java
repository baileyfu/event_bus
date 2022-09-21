package io.github.eventbus.constants;

import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * @author ALi
 * @version 1.0
 * @date 2022-09-08 15:33
 * @description
 */
public class JSONConfig {
    public static final SerializerFeature[] SERIALIZER_FEATURE_ARRAY = new SerializerFeature[]{SerializerFeature.QuoteFieldNames, SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat};
    public static final Feature[] FEATURE_ARRAY = new Feature[]{Feature.OrderedField};
}
