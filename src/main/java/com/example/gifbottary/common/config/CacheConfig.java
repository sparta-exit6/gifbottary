package com.example.gifbottary.common.config;

import com.example.gifbottary.domain.product.dto.response.ProductSearchPageResponse;
import com.example.gifbottary.domain.search.dto.response.PopularKeywordResponse;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 검색 결과와 인기 검색어 조회 결과를 Redis Remote Cache로 관리하는 설정입니다.
 * 기존 Caffeine Local Cache 대신 RedisCacheManager를 사용합니다.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PRODUCT_SEARCH_V2_CACHE = "productSearchV2";
    public static final String POPULAR_KEYWORD_CACHE = "popularKeyword";

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, JsonMapper jsonMapper) {
        // JavaTimeModule 등록 불필요 - Jackson 3는 java.time을 기본 지원
        RedisCacheConfiguration baseConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        cacheConfigurations.put(
                PRODUCT_SEARCH_V2_CACHE,
                baseConfig.entryTtl(Duration.ofMinutes(5))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                new JacksonJsonRedisSerializer<>(jsonMapper, ProductSearchPageResponse.class)
                        ))
        );

        JavaType popularKeywordListType = jsonMapper.getTypeFactory()
                .constructCollectionType(List.class, PopularKeywordResponse.class);

        cacheConfigurations.put(
                POPULAR_KEYWORD_CACHE,
                baseConfig.entryTtl(Duration.ofMinutes(3))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                new JacksonJsonRedisSerializer<>(jsonMapper, popularKeywordListType)
                        ))
        );

        RedisCacheConfiguration defaultConfig = baseConfig.serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJacksonJsonRedisSerializer(jsonMapper)
                )
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
