package com.example.gifbottary.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 인기 검색어 조회 성능을 높이기 위한 로컬 캐시 설정입니다.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String POPULAR_KEYWORD_CACHE = "popularKeywordV2";
    public static final String PRODUCT_SEARCH_V2_CACHE = "productSearchV2";

    @Bean
    public CaffeineCacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                POPULAR_KEYWORD_CACHE,
                PRODUCT_SEARCH_V2_CACHE
        );

        // TTL : 5분
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(1000));

        return cacheManager;
    }
}
