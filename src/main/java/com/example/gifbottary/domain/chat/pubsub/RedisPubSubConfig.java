package com.example.gifbottary.domain.chat.pubsub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * 다중 서버 환경에서 채팅 메시지 브로드캐스팅을 위해
 * Redis Pub/Sub 리스너 컨테이너를 설정하는 클래스입니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisPubSubConfig {

    private final RedisChatSubscriber redisChatSubscriber;

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer() {
            @Override
            public void start() {
                try {
                    super.start();
                } catch (Exception e) {
                    log.warn("Redis Pub/Sub 컨테이너 초기 연결 실패 (Redis 미구동 또는 접속 불가): {}", e.getMessage());
                }
            }
        };
        container.setConnectionFactory(connectionFactory);
        // chat-room:{roomId} 패턴으로 발행되는 모든 Redis 채널 구독
        container.addMessageListener(redisChatSubscriber, new PatternTopic("chat-room:*"));
        return container;
    }
}
