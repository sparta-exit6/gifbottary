package com.example.gifbottary.domain.search.service;

import com.example.gifbottary.common.config.CacheConfig;
import com.example.gifbottary.common.exception.ErrorCode;
import com.example.gifbottary.common.exception.ServiceException;
import com.example.gifbottary.domain.product.dto.request.ProductSearchRequest;
import com.example.gifbottary.domain.search.dto.response.PopularKeywordResponse;
import com.example.gifbottary.domain.search.dto.response.RecentKeywordResponse;
import com.example.gifbottary.domain.search.entity.SearchKeyword;
import com.example.gifbottary.domain.search.repository.SearchKeywordRepository;
import com.example.gifbottary.domain.user.entity.User;
import com.example.gifbottary.domain.user.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 상품 검색과 인기/최근 검색어 기능을 담당하는 서비스 클래스입니다.
 * 현재 팀 구조에서는 구현체가 하나뿐이므로 인터페이스 없이 단일 클래스로 관리합니다.
 */
@Service
public class SearchService {

    private final SearchKeywordRepository searchKeywordRepository;
    private final UserRepository userRepository;

    public SearchService(
            SearchKeywordRepository searchKeywordRepository,
            UserRepository userRepository
    ) {
        this.searchKeywordRepository = searchKeywordRepository;
        this.userRepository = userRepository;
    }

    /**
     * 상품 검색을 수행하고, 로그인 사용자의 검색어는 최근/인기 검색어 집계 대상으로 저장합니다.
     */
    @CacheEvict(
            cacheNames = CacheConfig.POPULAR_KEYWORD_CACHE,
            allEntries = true,
            condition = "#request.keyword() != null && !#request.keyword().isBlank()"
    )
    @Transactional
    public void saveSearchKeyword(Long userId, ProductSearchRequest request) {
        if (userId != null && request.keyword() != null && !request.keyword().isBlank()) {
            saveKeyword(userId, request.keyword().trim());
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.POPULAR_KEYWORD_CACHE, key = "#limit")
    public List<PopularKeywordResponse> findPopularKeywords(int limit) {
        if (limit < 1) {
            throw new ServiceException(ErrorCode.INVALID_QUERY_PARAMETER);
        }

        AtomicInteger rank = new AtomicInteger(1);
        return searchKeywordRepository.findPopularKeywords(PageRequest.of(0, limit))
                .stream()
                .map(projection -> new PopularKeywordResponse(
                        rank.getAndIncrement(),
                        projection.getKeyword(),
                        projection.getTotalCount().intValue()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecentKeywordResponse> findRecentKeywords(Long userId) {
        findUser(userId);
        return searchKeywordRepository.findTop10ByUser_IdOrderByLastSearchedAtDesc(userId)
                .stream()
                .map(RecentKeywordResponse::from)
                .toList();
    }

    @Transactional
    public void removeRecentKeyword(Long userId, String keyword) {
        findUser(userId);
        searchKeywordRepository.findByUser_IdAndKeyword(userId, keyword)
                .orElseThrow(() -> new ServiceException(ErrorCode.RECENT_KEYWORD_NOT_FOUND));
        searchKeywordRepository.deleteByUser_IdAndKeyword(userId, keyword);
    }

    @Transactional
    public void removeAllRecentKeywords(Long userId) {
        findUser(userId);
        searchKeywordRepository.deleteAllByUser_Id(userId);
    }

    /**
     * 같은 사용자의 동일 검색어는 하나의 row로 유지하고, 검색 횟수만 증가시킵니다.
     */
    private void saveKeyword(Long userId, String keyword) {
        User user = findUser(userId);
        searchKeywordRepository.findByUser_IdAndKeyword(userId, keyword)
                .ifPresentOrElse(
                        SearchKeyword::increaseCount,
                        () -> searchKeywordRepository.save(new SearchKeyword(user, keyword))
                );
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));
    }
}
