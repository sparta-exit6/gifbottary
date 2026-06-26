package com.example.gifbottary.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 인기 검색어 조회 성능을 높이기 위한 로컬 캐시 설정입니다.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String POPULAR_KEYWORD_CACHE = "popularKeywords";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(POPULAR_KEYWORD_CACHE);
        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(10))
                        .maximumSize(100)
        );
        return cacheManager;
    }
}
