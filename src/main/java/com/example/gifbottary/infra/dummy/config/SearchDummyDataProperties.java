package com.example.gifbottary.infra.dummy.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 검색 성능 테스트용 대용량 더미 데이터 적재 설정입니다.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "dummy.search")
public class SearchDummyDataProperties {

    /**
     * true일 때 애플리케이션 시작 시 더미 데이터를 적재합니다.
     */
    private boolean enabled = false;

    /**
     * 적재할 판매글 수입니다.
     */
    private int size = 50_000;

    /**
     * JDBC batch insert 단위입니다.
     */
    private int batchSize = 1_000;

    /**
     * 적재 전 기존 더미 데이터를 삭제할지 여부입니다.
     */
    private boolean cleanupBeforeLoad = true;

    /**
     * 더미 판매자 이메일입니다.
     */
    private String sellerEmail = "dummy-seller@gifbottary.local";

    /**
     * 더미 판매자 이름입니다.
     */
    private String sellerName = "더미 판매자";

    /**
     * 더미 상품명 prefix입니다.
     */
    private String productNamePrefix = "DUMMY-SEARCH";
}