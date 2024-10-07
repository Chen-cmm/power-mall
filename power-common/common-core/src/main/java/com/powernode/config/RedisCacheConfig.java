package com.powernode.config;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;

/**
 * Redis缓存配置类
 */
public class RedisCacheConfig {
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(){
        //创建redis缓存配置类
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();
        //进行redis配置
        redisCacheConfiguration.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.java()))//设置redis值序列化的方式为json格式
                .entryTtl(Duration.ofDays(7))//同一设置redis值的默认过期时间
                .disableCachingNullValues();//禁止缓存空值
        return redisCacheConfiguration;
    }
}
