package com.finalbid.user.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.StringCodec;
import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis configuration for user-service.
 * Sets up Lettuce connection factory and Bucket4j proxy manager
 * for distributed rate limiting.
 */
@Configuration
public class RedisConfig {

    @Value("${REDIS_HOST:localhost}")
    private String redisHost;

    @Value("${REDIS_PORT:6379}")
    private int redisPort;

    @Bean
    public LettuceBasedProxyManager<String> lettuceProxyManager() {
        RedisClient redisClient = RedisClient.create("redis://" + redisHost + ":" + redisPort);
        StatefulRedisConnection<String, byte[]> connection =
            redisClient.connect(io.lettuce.core.codec.RedisCodec.of(
                StringCodec.UTF8,
                io.lettuce.core.codec.ByteArrayCodec.INSTANCE
            ));
        return LettuceBasedProxyManager.builderFor(connection)
            .withClientSideConfig(ClientSideConfig.getDefault())
            .build();
    }
}
