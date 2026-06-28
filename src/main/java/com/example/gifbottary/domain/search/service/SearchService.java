package com.example.gifbottary.domain.search.service;

import com.example.gifbottary.common.config.CacheConfig;
import com.example.gifbottary.common.exception.ErrorCode;
import com.example.gifbottary.common.exception.ServiceException;
import com.example.gifbottary.domain.product.dto.request.ProductSearchRequest;
import com.example.gifbottary.domain.search.config.PopularSearchProperties;
import com.example.gifbottary.domain.search.dto.response.PopularKeywordResponse;
import com.example.gifbottary.domain.search.dto.response.RecentKeywordResponse;
import com.example.gifbottary.domain.search.entity.SearchKeyword;
import com.example.gifbottary.domain.search.repository.SearchKeywordRepository;
import com.example.gifbottary.domain.user.entity.User;
import com.example.gifbottary.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 상품 검색과 연결되는 최근 검색어, 인기 검색어 기능을 담당하는 서비스입니다.
 * 최근 검색어는 DB에 저장하고, 인기 검색어는 Redis ZSet으로 관리하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchKeywordRepository searchKeywordRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final PopularSearchProperties popularSearchProperties;

    /**
     * 현재 정책
     * 1. 비로그인 사용자는 인기 검색어 집계에서 제외
     * 2. 최근 검색어는 DB에 저장
     * 3. 인기 검색어는 Redis ZSet 점수 증가
     *
     * @param userId
     * @param request
     */
    @CacheEvict(
            cacheNames = CacheConfig.POPULAR_KEYWORD_CACHE,
            allEntries = true,
            condition = "#request != null && (#request.hasKeyword() || #request.hasBrand())"
    )
    @Transactional
    public void saveSearchKeyword(Long userId, ProductSearchRequest request) {
        // 로그인 사용자가 아니거나 요청이 없으면 저장하지 않음
        if (userId == null || request == null) {
            return;
        }

        // keyword가 있으면 keyword를,
        // 없으면 brand를 대표 검색어로 사용
        String keyword = resolveSearchKeyword(request);

        // 저장할 검색어가 없으면 종료
        if (keyword == null) {
            return;
        }

        // "스타벅스" 와 " 스타벅스 " 를 같은 검색어로 보기 위해 공백 정리
        String normalizedKeyword = normalizeKeyword(keyword);

        // 최근 검색어는 DB에 저장
        saveKeyword(userId, normalizedKeyword);

        // 인기 검색어는 Redis ZSet 점수 증가
        increasePopularKeywordScore(userId, normalizedKeyword);
    }

    /**
     * 캐시 없이 Redis에서 직접 인기 검색어를 조회하는 v1 메서드입니다.
     */
    @Transactional(readOnly = true)
    public List<PopularKeywordResponse> findPopularKeywordsV1(int limit) {
        return getPopularKeywords(limit);
    }

    /**
     * Redis 조회 결과를 로컬 캐시에 저장하는 v2 메서드입니다.
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.POPULAR_KEYWORD_CACHE, key = "'popular:v2:limit:' + #limit")
    public List<PopularKeywordResponse> findPopularKeywordsV2(int limit) {
        return getPopularKeywords(limit);
    }

    /**
     * 로그인 사용자의 최근 검색어를 조회합니다.
     *
     * @param userId
     * @return
     */
    @Transactional(readOnly = true)
    public List<RecentKeywordResponse> findRecentKeywords(Long userId) {
        findUser(userId);

        return searchKeywordRepository.findTop10ByUser_IdOrderByLastSearchedAtDesc(userId)
                .stream()
                .map(RecentKeywordResponse::from)
                .toList();
    }

    /**
     * 특정 최근 검색어를 삭제합니다.
     *
     * @param userId
     * @param keyword
     */
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
     * 최근 검색어는 같은 사용자가 같은 검색어를 동시에 여러 번 요청해도 DB row하나를 유지하면서 검색 횟수와
     * 최근 검색 시각을 갱신합니다.
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

    /**
     * 최근 검색어 row가 없을 때 새로 저장합니다.
     *
     * @param user
     * @param keyword
     */
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
        // 잘못된 limit 요청 방지
        if (limit < 1) {
            throw new ServiceException(ErrorCode.INVALID_QUERY_PARAMETER);
        }

        // 점수가 높은 순으로 상위 N개 조회(ZREVRANGE 방식)
        Set<ZSetOperations.TypedTuple<String>> tuples = stringRedisTemplate.opsForZSet()
                .reverseRangeWithScores(getDailyPopularKey(), 0, limit - 1);

        // 인기 검색어가 없으면 빈 리스트 반환
        if (tuples == null || tuples.isEmpty()) {
            return List.of();
        }

        // 인기 검색어 응답에 1위부터 순위를 부여하기 위한 카운터
        AtomicInteger rank = new AtomicInteger(1);
        List<PopularKeywordResponse> responses = new ArrayList<>();

        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            if (tuple.getValue() == null || tuple.getScore() == null) {
                continue;
            }

            responses.add(new PopularKeywordResponse(
                    rank.getAndIncrement(),
                    tuple.getValue(),
                    tuple.getScore().intValue()
            ));
        }

        return responses;
    }

    /**
     * 검색 요청에서 대표 검색어를 추출합니다.
     * 우선 순위
     * 1. keyword
     * 2. brand
     *
     * @param request
     * @return
     */
    private String resolveSearchKeyword(ProductSearchRequest request) {
        if (request.hasKeyword()) {
            return request.keyword();
        }
        if (request.hasBrand()) {
            return request.brand();
        }
        return null;
    }

    /**
     * 사용자를 조회합니다.
     *
     * @param userId
     * @return
     */
    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 오늘 날짜 기준 인기 검색어 ZSet Key 생성합니다.
     * 예 : popular:keywords:daily:2026-06-29
     *
     * @return
     */
    private String getDailyPopularKey() {
        return popularSearchProperties.getDailyKeyPrefix() + ":" + LocalDate.now();
    }

    /**
     * 같은 사용자의 같은 검색어 중복 집계를 막기 위한 dedupe key를 생성합니다.
     * 예 : popular:dedupe:user:3:keyword:스타벅스:2026-06-29
     *
     * @param userId
     * @param keyword
     * @return
     */
    private String getDedupeKey(Long userId, String keyword) {
        return popularSearchProperties.getDedupeKeyPrefix()
                + ":user:" + userId
                + ":keyword:" + keyword
                + ":" + LocalDate.now();
    }

    // 검색어 공백 정리 "스타벅스"와 " 스타벅스 " 같은 검색어로 처리
    private String normalizeKeyword(String keyword) {
        return keyword.trim();
    }

    /**
     * 같은 사용자의 동일 검색어는 일정 시간 내 1회만 인기 검색어 점수에 반영합니다.
     * dedupe key 확인 -> 없으면 ZSet 점수 + 1 있으면 dedupe key TTL 저장
     */
    private void increasePopularKeywordScore(Long userId, String keyword) {
        String dedupeKey = getDedupeKey(userId, keyword);

        // dedupe key가 이미 있다면 최근에 같은 사용자가 같은 검색어를 검색한 상태
        Boolean exists = stringRedisTemplate.hasKey(dedupeKey);

        if (Boolean.TRUE.equals(exists)) {
            return;
        }

        String dailyPopularKey = getDailyPopularKey();

        // ZSet 점수 1증가
        stringRedisTemplate.opsForZSet().incrementScore(
                dailyPopularKey,
                keyword,
                popularSearchProperties.getScoreIncrement()
        );

        // dedupe key를 TTL과 함께 저장해서 일정 시간 동안 중복 집계 방지
        stringRedisTemplate.opsForValue().set(dedupeKey, "1", Duration.ofMinutes(popularSearchProperties.getDedupeTtlMinutes()));
    }
}