package com.example.gifbottary.domain.product.controller;

import com.example.gifbottary.common.exception.GlobalExceptionHandler;
import com.example.gifbottary.domain.product.dto.request.ProductCreateRequest;
import com.example.gifbottary.domain.product.dto.response.PinDetailResponse;
import com.example.gifbottary.domain.product.dto.response.ProductCreateResponse;
import com.example.gifbottary.domain.product.dto.response.ProductPinValidationResponse;
import com.example.gifbottary.domain.product.dto.response.ProductSummaryResponse;
import com.example.gifbottary.domain.product.service.ProductService;
import com.example.gifbottary.domain.search.service.SearchService;
import com.example.gifbottary.domain.user.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @Mock
    private SearchService searchService;

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new AuthenticationPrincipalArgumentResolver()
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("상품 등록이 성공하면 생성 응답을 반환한다")
    void createProduct_success() throws Exception {
        authenticate(1L, "USER");

        ProductCreateResponse response = new ProductCreateResponse(
                1L,
                101L,
                "PERSONAL",
                "스타벅스",
                "아메리카노 T",
                4500,
                LocalDate.of(2026, 12, 31),
                4000,
                0,
                "PENDING_REVIEW",
                "https://example.com/image.png",
                LocalDateTime.of(2026, 6, 24, 20, 0)
        );

        given(productService.createProduct(eq(1L), any(ProductCreateRequest.class))).willReturn(response);

        String requestBody = """
                {
                  "saleType": "PERSONAL",
                  "brand": "스타벅스",
                  "productName": "아메리카노 T",
                  "faceValue": 4500,
                  "expireAt": "2026-12-31",
                  "salePrice": 4000,
                  "pinNumber": "1111-2222-3333",
                  "pinNumbers": ["1111-2222-3333"],
                  "imageUrl": "https://example.com/image.png"
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.saleId").value(1))
                .andExpect(jsonPath("$.data.productId").value(101))
                .andExpect(jsonPath("$.data.saleStatus").value("PENDING_REVIEW"))
                .andExpect(jsonPath("$.data.pinCheckStatus").doesNotExist());
    }

    @Test
    @DisplayName("상품 등록 시 인증 정보가 없으면 401을 반환한다")
    void createProduct_withoutAuthentication_unauthorized() throws Exception {
        String requestBody = """
                {
                  "saleType": "PERSONAL",
                  "brand": "스타벅스",
                  "productName": "아메리카노 T",
                  "faceValue": 4500,
                  "expireAt": "2026-12-31",
                  "salePrice": 4000,
                  "pinNumber": "1111-2222-3333",
                  "pinNumbers": ["1111-2222-3333"],
                  "imageUrl": "https://example.com/image.png"
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_001"));
    }

    @Test
    @DisplayName("판매글 전체 조회는 인증 없이 성공한다")
    void findProducts_withoutAuthentication_success() throws Exception {
        ProductSummaryResponse summary = new ProductSummaryResponse(
                1L,
                101L,
                "PERSONAL",
                "스타벅스",
                "아메리카노 T",
                4500,
                LocalDate.of(2026, 12, 31),
                4000,
                0,
                "ON_SALE",
                "https://example.com/image.png"
        );

        given(productService.findProducts(any(), any()))
                .willReturn(new PageImpl<>(List.of(summary), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].saleId").value(1))
                .andExpect(jsonPath("$.data.content[0].productId").value(101))
                .andExpect(jsonPath("$.data.content[0].saleType").value("PERSONAL"));
    }

    @Test
    @DisplayName("핀 검수 상태 조회는 JWT 인증 사용자가 있으면 성공한다")
    void findPinValidation_success() throws Exception {
        authenticate(1L, "USER");

        ProductPinValidationResponse response = new ProductPinValidationResponse(
                1L,
                "PLATFORM",
                "PENDING_REVIEW",
                2,
                1,
                1,
                0,
                0,
                List.of(
                        new PinDetailResponse(10L, "PENDING", "AVAILABLE", LocalDateTime.now(), LocalDateTime.now()),
                        new PinDetailResponse(11L, "VALID", "AVAILABLE", LocalDateTime.now(), LocalDateTime.now())
                )
        );

        given(productService.findPinValidation(1L, 1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/products/1/pin-validation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.saleId").value(1))
                .andExpect(jsonPath("$.data.totalPinCount").value(2))
                .andExpect(jsonPath("$.data.pendingCount").value(1))
                .andExpect(jsonPath("$.data.validCount").value(1));
    }

    private void authenticate(Long userId, String role) {
        User user = new User("user@test.com", "password", "사용자", role, 0);
        ReflectionTestUtils.setField(user, "id", userId);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}