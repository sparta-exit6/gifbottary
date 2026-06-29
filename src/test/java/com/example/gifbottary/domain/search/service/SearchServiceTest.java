package com.example.gifbottary.domain.search.service;

import com.example.gifbottary.domain.product.dto.request.ProductSearchRequest;
import com.example.gifbottary.domain.search.config.PopularSearchProperties;
import com.example.gifbottary.domain.search.dto.response.PopularKeywordResponse;
import com.example.gifbottary.domain.search.entity.SearchKeyword;
import com.example.gifbottary.domain.search.repository.SearchKeywordRepository;
import com.example.gifbottary.domain.user.entity.Role;
import com.example.gifbottary.domain.user.entity.User;
import com.example.gifbottary.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private SearchKeywordRepository searchKeywordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private PopularSearchProperties popularSearchProperties;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private SearchService searchService;

    @Test
    @DisplayName("Redis 장애가 발생해도 검색어 저장은 실패하지 않고 인기 검색어 집계만 건너뛴다")
    void saveSearchKeyword_whenRedisFails_searchRequestDoesNotFail() {
        User user = createUser(1L);
        ProductSearchRequest request = new ProductSearchRequest("스타벅스", null, null, null);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(searchKeywordRepository.findByUser_IdAndKeyword(1L, "스타벅스"))
                .willReturn(Optional.empty());
        given(stringRedisTemplate.hasKey(anyString()))
                .willThrow(new DataAccessResourceFailureException("Redis down"));

        assertThatCode(() -> searchService.saveSearchKeyword(1L, request))
                .doesNotThrowAnyException();

        verify(searchKeywordRepository).saveAndFlush(any(SearchKeyword.class));
    }

    @Test
    @DisplayName("brand만 전달되어도 최근 검색어와 인기 검색어 집계 대상으로 처리한다")
    void saveSearchKeyword_withBrandOnly_usesBrandAsKeyword() {
        User user = createUser(1L);
        ProductSearchRequest request = new ProductSearchRequest(null, "배스킨라빈스", null, null);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(searchKeywordRepository.findByUser_IdAndKeyword(1L, "배스킨라빈스"))
                .willReturn(Optional.empty());
        given(popularSearchProperties.getDedupeKeyPrefix()).willReturn("popular:dedupe");
        given(popularSearchProperties.getDailyKeyPrefix()).willReturn("popular:keywords:daily");
        given(popularSearchProperties.getScoreIncrement()).willReturn(1.0);
        given(popularSearchProperties.getDedupeTtlMinutes()).willReturn(10L);
        given(stringRedisTemplate.hasKey(anyString())).willReturn(false);
        given(stringRedisTemplate.opsForZSet()).willReturn(zSetOperations);
        given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);

        searchService.saveSearchKeyword(1L, request);

        verify(searchKeywordRepository).saveAndFlush(any(SearchKeyword.class));
        verify(zSetOperations).incrementScore(anyString(), anyString(), any(Double.class));
        verify(valueOperations).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("비로그인 사용자는 최근 검색어와 인기 검색어 집계 대상에서 제외한다")
    void saveSearchKeyword_whenUserIdIsNull_returnsImmediately() {
        ProductSearchRequest request = new ProductSearchRequest("스타벅스", null, null, null);

        assertThatCode(() -> searchService.saveSearchKeyword(null, request))
                .doesNotThrowAnyException();

        verify(searchKeywordRepository, never()).findByUser_IdAndKeyword(anyLong(), anyString());
        verify(stringRedisTemplate, never()).hasKey(anyString());
    }

    @Test
    @DisplayName("Redis 장애가 발생하면 인기 검색어 조회는 빈 리스트를 반환한다")
    void findPopularKeywordsV1_whenRedisFails_returnsEmptyList() {
        given(stringRedisTemplate.opsForZSet()).willReturn(zSetOperations);
        given(popularSearchProperties.getDailyKeyPrefix()).willReturn("popular:keywords:daily");
        given(zSetOperations.reverseRangeWithScores(anyString(), anyLong(), anyLong()))
                .willThrow(new DataAccessResourceFailureException("Redis down"));

        List<PopularKeywordResponse> result = searchService.findPopularKeywordsV1(10);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Redis 인기 검색어 조회가 성공하면 랭킹 목록을 반환한다")
    void findPopularKeywordsV1_whenRedisSucceeds_returnsRankedKeywords() {
        ZSetOperations.TypedTuple<String> tuple1 = ZSetOperations.TypedTuple.of("스타벅스", 3.0);
        ZSetOperations.TypedTuple<String> tuple2 = ZSetOperations.TypedTuple.of("배스킨라빈스", 1.0);

        given(stringRedisTemplate.opsForZSet()).willReturn(zSetOperations);
        given(popularSearchProperties.getDailyKeyPrefix()).willReturn("popular:keywords:daily");
        given(zSetOperations.reverseRangeWithScores(anyString(), anyLong(), anyLong()))
                .willReturn(Set.of(tuple1, tuple2));

        List<PopularKeywordResponse> result = searchService.findPopularKeywordsV1(10);

        assertThat(result).hasSize(2);
        assertThat(result.stream().map(PopularKeywordResponse::keyword))
                .contains("스타벅스", "배스킨라빈스");
    }

    private User createUser(Long id) {
        User user = new User("user@test.com", "password", "사용자", Role.USER, 0);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}