package com.example.gifbottary.domain.search.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 인기 검색어 Redis 집계 정책 설정값을 관리하는 클래스입니다.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "search.popular")
public class PopularSearchProperties {

    /**
     * 같은 사용자의 동일 검색어를 중복 집계하지 않는 시간(분)입니다.
     */
    private long dedupeTtlMinutes;

    /**
     * 검색 1회당 증가할 점수입니다.
     */
    private double scoreIncrement;

    /**
     * 일별 인기 검색어 Redis key prefix입니다.
     * 예: popular:keywords:daily
     */
    private String dailyKeyPrefix;

    /**
     * dedupe key Redis prefix입니다.
     * 예: popular:dedupe
     */
    private String dedupeKeyPrefix;

}