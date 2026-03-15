package com.finalbid.auction.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.StringCodec;
import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import com.finalbid.auction.websocket.BidWebSocketListener;

@Configuration
public class RedisConfig {

    @Value("${REDIS_HOST:localhost}")
    private String redisHost;

    @Value("${REDIS_PORT:6379}")
    private int redisPort;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    @Bean
    public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

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

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                                MessageListenerAdapter bidListenerAdapter,
                                                MessageListenerAdapter endedListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(bidListenerAdapter, new PatternTopic("finalbid:ws:bid:*"));
        container.addMessageListener(endedListenerAdapter, new PatternTopic("finalbid:ws:ended:*"));
        return container;
    }

    @Bean
    public MessageListenerAdapter bidListenerAdapter(BidWebSocketListener listener) {
        return new MessageListenerAdapter(listener, "handleBidMessage");
    }

    @Bean
    public MessageListenerAdapter endedListenerAdapter(BidWebSocketListener listener) {
        return new MessageListenerAdapter(listener, "handleEndedMessage");
    }
}
