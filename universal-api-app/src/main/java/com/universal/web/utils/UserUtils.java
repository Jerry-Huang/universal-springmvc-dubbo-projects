package com.universal.web.utils;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.universal.api.user.bean.UserBean;

@Component
public class UserUtils {

    private static final int CACHED_DAYS = 30;
    private static final String CACHED_USER_BY_TOKEN = "TTL30D:USER-BY-TOKEN#%s";

    @Resource
    private RedisTemplate<String, Serializable> redisTemplate;
    private static RedisTemplate<String, Serializable> staticRedisTemplate;

    @PostConstruct
    public void init() {
        staticRedisTemplate = redisTemplate;

    }

    public static UserBean findCachedUser(final String token) {

        String key = String.format(CACHED_USER_BY_TOKEN, token);
        return (UserBean) staticRedisTemplate.opsForValue().get(key);
    }

    public static void cacheUser(final String token, final UserBean userBean) {

        String key = String.format(CACHED_USER_BY_TOKEN, token);
        staticRedisTemplate.opsForValue().set(key, userBean, CACHED_DAYS, TimeUnit.DAYS);
    }
}
