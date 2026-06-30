package com.example.gifbottary.common.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 검색 결과와 인기 검색어 조회 결과를 Redis Remote Cache로 관리하는 설정입니다.
 * 기존 Caffeine Local Cache 대신 RedisCacheManager를 사용합니다.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String POPULAR_KEYWORD_CACHE = "popularKeywordV2";
    public static final String PRODUCT_SEARCH_V2_CACHE = "productSearchV2";

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        GenericJacksonJsonRedisSerializer valueSerializer =
                GenericJacksonJsonRedisSerializer.create(builder -> builder
                        .enableSpringCacheNullValueSupport()
                        .enableUnsafeDefaultTyping()
                );

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                // Redis key는 사람이 읽을 수 있는 문자열 형태로 저장합니다.
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                // Redis value는 JSON으로 직렬화해 저장합니다.
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer)
                )
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 상품 검색 결과 캐시는 5분 유지
        cacheConfigurations.put(
                PRODUCT_SEARCH_V2_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(5))
        );

        // 인기 검색어 조회 결과 캐시는 3분 유지
        cacheConfigurations.put(
                POPULAR_KEYWORD_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(3))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
