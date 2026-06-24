package com.example.gifbottary.domain.search.dto.response;

public record PopularKeywordResponse(
        Integer rank,
        String keyword,
        Integer searchCount
) {
}
