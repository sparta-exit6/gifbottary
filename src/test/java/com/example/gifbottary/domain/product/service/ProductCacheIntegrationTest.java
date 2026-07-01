package com.example.gifbottary.domain.product.service;

import com.example.gifbottary.common.config.CacheConfig;
import com.example.gifbottary.common.util.PinEncryptor;
import com.example.gifbottary.domain.product.dto.request.ProductSearchRequest;
import com.example.gifbottary.domain.product.dto.response.ProductSearchPageResponse;
import com.example.gifbottary.domain.product.dto.response.ProductSummaryResponse;
import com.example.gifbottary.domain.product.enums.SaleStatus;
import com.example.gifbottary.domain.product.enums.SaleType;
import com.example.gifbottary.domain.product.repository.GifticonPinRepository;
import com.example.gifbottary.domain.product.repository.GifticonProductRepository;
import com.example.gifbottary.domain.product.repository.GifticonSaleRepository;
import com.example.gifbottary.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = {ProductService.class, ProductCacheIntegrationTest.ProductCacheTestConfig.class})
class ProductCacheIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private GifticonSaleRepository gifticonSaleRepository;

    @MockitoBean
    private GifticonProductRepository gifticonProductRepository;

    @MockitoBean
    private GifticonPinRepository gifticonPinRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PinEncryptor pinEncryptor;

    @BeforeEach
    void setUp() {
        Cache cache = cacheManager.getCache(CacheConfig.PRODUCT_SEARCH_V2_CACHE);
        Objects.requireNonNull(cache).clear();
    }

    @Test
    @DisplayName("동일한 v2 검색 요청은 두 번째부터 캐시를 사용한다")
    void searchProductsV2_sameRequest_usesCache() {
        ProductSearchRequest request = new ProductSearchRequest("스타벅스", null, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductSummaryResponse> expected = new PageImpl<>(
                List.of(new ProductSummaryResponse(
                        1L,
                        10L,
                        SaleType.PERSONAL,
                        "스타벅스",
                        "아메리카노 T",
                        4500,
                        LocalDate.of(2026, 12, 31),
                        4000,
                        1,
                        SaleStatus.ON_SALE,
                        "https://example.com/image.png"
                )),
                pageable,
                1
        );

        given(gifticonSaleRepository.searchProducts(request, pageable)).willReturn(expected);

        ProductSearchPageResponse first = productService.searchProductsV2(request, pageable);
        ProductSearchPageResponse second = productService.searchProductsV2(request, pageable);

        assertThat(first.content()).hasSize(1);
        assertThat(second.content()).hasSize(1);
        assertThat(first.totalElements()).isEqualTo(1L);
        assertThat(second.totalElements()).isEqualTo(1L);
        verify(gifticonSaleRepository, times(1)).searchProducts(request, pageable);
    }

    @Test
    @DisplayName("페이지가 달라지면 다른 캐시 키로 인식해 다시 조회한다")
    void searchProductsV2_differentPage_usesDifferentCacheKey() {
        ProductSearchRequest request = new ProductSearchRequest("스타벅스", null, null, null);
        Pageable firstPage = PageRequest.of(0, 10);
        Pageable secondPage = PageRequest.of(1, 10);

        given(gifticonSaleRepository.searchProducts(request, firstPage)).willReturn(Page.empty(firstPage));
        given(gifticonSaleRepository.searchProducts(request, secondPage)).willReturn(Page.empty(secondPage));

        productService.searchProductsV2(request, firstPage);
        productService.searchProductsV2(request, secondPage);

        verify(gifticonSaleRepository, times(1)).searchProducts(request, firstPage);
        verify(gifticonSaleRepository, times(1)).searchProducts(request, secondPage);
    }

    @TestConfiguration
    @EnableCaching
    static class ProductCacheTestConfig {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(CacheConfig.PRODUCT_SEARCH_V2_CACHE);
        }
    }
}
