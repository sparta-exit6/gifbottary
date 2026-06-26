package com.example.gifbottary.domain.search.controller;

import com.example.gifbottary.common.response.ApiResponse;
import com.example.gifbottary.domain.search.dto.response.PopularKeywordResponse;
import com.example.gifbottary.domain.search.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 검색 v2 API입니다.
 * 인기 검색어 조회에 로컬 메모리 캐시를 적용한 버전입니다.
 */
@RestController
@RequestMapping("/api/v2/search")
public class SearchV2Controller {

    private final SearchService searchService;

    public SearchV2Controller(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/popular-keywords")
    public ResponseEntity<ApiResponse<List<PopularKeywordResponse>>> findPopularKeywords(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.ok(searchService.findPopularKeywordsV2(limit)));
    }
}