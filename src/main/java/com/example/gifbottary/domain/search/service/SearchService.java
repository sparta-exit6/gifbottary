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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 상품 검색과 연결되는 최근 검색어, 인기 검색어 기능을 담당하는 서비스입니다.
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

    @CacheEvict(
            cacheNames = CacheConfig.POPULAR_KEYWORD_CACHE,
            allEntries = true,
            condition = "#request != null && (#request.hasKeyword() || #request.hasBrand())"
    )
    @Transactional
    public void saveSearchKeyword(Long userId, ProductSearchRequest request) {
        if (userId == null || request == null) {
            return;
        }

        String keyword = resolveSearchKeyword(request);
        if (keyword == null) {
            return;
        }

        saveKeyword(userId, keyword);
    }

    /**
     * v1 인기 검색어 조회입니다.
     * 캐시를 사용하지 않고 매 요청마다 DB 집계 결과를 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<PopularKeywordResponse> findPopularKeywordsV1(int limit) {
        return getPopularKeywords(limit);
    }

    /**
     * v2 인기 검색어 조회입니다.
     * limit 기준으로 로컬 메모리 캐시를 사용합니다.
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.POPULAR_KEYWORD_CACHE, key = "#limit")
    public List<PopularKeywordResponse> findPopularKeywordsV2(int limit) {
        return getPopularKeywords(limit);
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
     * 같은 사용자가 같은 검색어를 동시에 여러 번 요청해도
     * unique 제약 예외로 실패하지 않도록 저장 로직을 보완합니다.
     */
    private void saveKeyword(Long userId, String keyword) {
        User user = findUser(userId);
        searchKeywordRepository.findByUser_IdAndKeyword(userId, keyword)
                .ifPresentOrElse(
                        SearchKeyword::increaseCount,
                        () -> saveKeywordWhenAbsent(user, keyword)
                );
    }

    private void saveKeywordWhenAbsent(User user, String keyword) {
        try {
            searchKeywordRepository.saveAndFlush(new SearchKeyword(user, keyword));
        } catch (DataIntegrityViolationException exception) {
            // 동시 요청으로 이미 insert 된 경우 조회 후 카운트만 증가시킵니다.
            searchKeywordRepository.findByUser_IdAndKeyword(user.getId(), keyword)
                    .ifPresent(SearchKeyword::increaseCount);
        }
    }

    private List<PopularKeywordResponse> getPopularKeywords(int limit) {
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

    private String resolveSearchKeyword(ProductSearchRequest request) {
        if (request.hasKeyword()) {
            return request.keyword().trim();
        }
        if (request.hasBrand()) {
            return request.brand().trim();
        }
        return null;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));
    }
}