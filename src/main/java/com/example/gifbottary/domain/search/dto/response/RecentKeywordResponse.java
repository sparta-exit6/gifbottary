package com.example.gifbottary.domain.search.dto.response;

import com.example.gifbottary.domain.search.entity.SearchKeyword;

import java.time.LocalDateTime;

public record RecentKeywordResponse(
        String keyword,
        LocalDateTime searchedAt
) {
    public static RecentKeywordResponse from(SearchKeyword searchKeyword) {
        return new RecentKeywordResponse(searchKeyword.getKeyword(), searchKeyword.getLastSearchedAt());
    }
}
