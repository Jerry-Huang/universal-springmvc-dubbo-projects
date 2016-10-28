package com.universal.cache;

import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

public class PrefixRedisKeySerializer extends StringRedisSerializer {

    private String prefix;
    private static final String SEPARATOR = ":";

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public byte[] serialize(String param) throws SerializationException {

        return super.serialize(prefix + SEPARATOR + param);
    }

}
