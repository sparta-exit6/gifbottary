package com.example.gifbottary.domain.product.service;

import com.example.gifbottary.common.exception.ServiceException;
import com.example.gifbottary.common.util.PinEncryptor;
import com.example.gifbottary.domain.product.dto.request.ProductCreateRequest;
import com.example.gifbottary.domain.product.dto.response.ProductCreateResponse;
import com.example.gifbottary.domain.product.entity.GifticonProduct;
import com.example.gifbottary.domain.product.entity.GifticonSale;
import com.example.gifbottary.domain.product.enums.SaleType;
import com.example.gifbottary.domain.product.repository.GifticonPinRepository;
import com.example.gifbottary.domain.product.repository.GifticonProductRepository;
import com.example.gifbottary.domain.product.repository.GifticonSaleRepository;
import com.example.gifbottary.domain.user.entity.Role;
import com.example.gifbottary.domain.user.entity.User;
import com.example.gifbottary.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private GifticonProductRepository gifticonProductRepository;

    @Mock
    private GifticonSaleRepository gifticonSaleRepository;

    @Mock
    private GifticonPinRepository gifticonPinRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PinEncryptor pinEncryptor;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("개인 판매 상품을 직접 입력하면 검수 대기 상태로 등록된다")
    void createProduct_withDirectProductInfo_savesPendingReviewSale() {
        User seller = createUser(1L, Role.ADMIN);

        given(userRepository.findById(1L)).willReturn(Optional.of(seller));
        given(gifticonProductRepository.findByBrandAndProductName("스타벅스", "아메리카노 T")).willReturn(Optional.empty());
        given(gifticonProductRepository.save(any(GifticonProduct.class))).willAnswer(invocation -> {
            GifticonProduct product = invocation.getArgument(0);
            ReflectionTestUtils.setField(product, "id", 101L);
            return product;
        });
        given(pinEncryptor.hash("1111-2222-3333")).willReturn("pin-hash");
        given(pinEncryptor.encrypt("1111-2222-3333")).willReturn("encrypted-pin");
        given(gifticonSaleRepository.save(any(GifticonSale.class))).willAnswer(invocation -> {
            GifticonSale sale = invocation.getArgument(0);
            ReflectionTestUtils.setField(sale, "id", 1001L);
            return sale;
        });

        ProductCreateRequest request = new ProductCreateRequest(
                null,
                SaleType.PERSONAL,
                "스타벅스",
                "아메리카노 T",
                4500,
                LocalDate.now().plusDays(10),
                4000,
                "1111-2222-3333",
                null,
                "https://example.com/image.png"
        );

        ProductCreateResponse response = productService.createProduct(1L, request);

        assertThat(response.saleId()).isEqualTo(1001L);
        assertThat(response.productId()).isEqualTo(101L);
        assertThat(response.saleType()).isEqualTo("PERSONAL");
        assertThat(response.saleStatus()).isEqualTo("PENDING_REVIEW");
        verify(gifticonProductRepository).save(any(GifticonProduct.class));
        verify(gifticonSaleRepository).save(any(GifticonSale.class));
    }

    @Test
    @DisplayName("플랫폼 판매에서 기존 상품 ID를 선택하면 기존 상품을 사용한다")
    void createProduct_withSelectedProductInPlatformSale_usesExistingProduct() {
        User seller = createUser(1L, Role.ADMIN);
        GifticonProduct existingProduct = new GifticonProduct("스타벅스", "아메리카노 T", 4500, "https://example.com/image.png");
        ReflectionTestUtils.setField(existingProduct, "id", 101L);

        given(userRepository.findById(1L)).willReturn(Optional.of(seller));
        given(gifticonProductRepository.findById(101L)).willReturn(Optional.of(existingProduct));
        given(pinEncryptor.hash("1111-2222-3333")).willReturn("pin-hash");
        given(pinEncryptor.encrypt("1111-2222-3333")).willReturn("encrypted-pin");
        given(gifticonSaleRepository.save(any(GifticonSale.class))).willAnswer(invocation -> {
            GifticonSale sale = invocation.getArgument(0);
            ReflectionTestUtils.setField(sale, "id", 1002L);
            return sale;
        });

        ProductCreateRequest request = new ProductCreateRequest(
                101L,
                SaleType.PLATFORM,
                null,
                null,
                null,
                LocalDate.now().plusDays(10),
                4000,
                "1111-2222-3333",
                null,
                null
        );

        ProductCreateResponse response = productService.createProduct(1L, request);

        assertThat(response.productId()).isEqualTo(101L);
        assertThat(response.saleType()).isEqualTo("PLATFORM");
        verify(gifticonProductRepository, never()).save(any(GifticonProduct.class));
    }

    @Test
    @DisplayName("플랫폼 판매를 일반 사용자가 등록하면 실패한다")
    void createProduct_withPlatformSaleByUser_throwsException() {
        User seller = createUser(1L, Role.USER);
        given(userRepository.findById(1L)).willReturn(Optional.of(seller));

        ProductCreateRequest request = new ProductCreateRequest(
                101L,
                SaleType.PLATFORM,
                null,
                null,
                null,
                LocalDate.now().plusDays(10),
                4000,
                "1111-2222-3333",
                null,
                null
        );

        assertThatThrownBy(() -> productService.createProduct(1L, request))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    @DisplayName("개인 판매에서 상품 ID를 보내면 실패한다")
    void createProduct_withProductIdInPersonalSale_throwsException() {
        User seller = createUser(1L, Role.USER);
        given(userRepository.findById(1L)).willReturn(Optional.of(seller));

        ProductCreateRequest request = new ProductCreateRequest(
                101L,
                SaleType.PERSONAL,
                null,
                null,
                null,
                LocalDate.now().plusDays(10),
                4000,
                "1111-2222-3333",
                null,
                null
        );

        assertThatThrownBy(() -> productService.createProduct(1L, request))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    @DisplayName("유효기간이 과거면 판매 등록에 실패한다")
    void createProduct_withPastExpireAt_throwsException() {
        User seller = createUser(1L, Role.USER);
        given(userRepository.findById(1L)).willReturn(Optional.of(seller));

        ProductCreateRequest request = new ProductCreateRequest(
                null,
                SaleType.PERSONAL,
                "스타벅스",
                "아메리카노 T",
                4500,
                LocalDate.now().minusDays(1),
                4000,
                "1111-2222-3333",
                null,
                null
        );

        assertThatThrownBy(() -> productService.createProduct(1L, request))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    @DisplayName("개인 판매에 핀 번호를 여러 개 등록하면 실패한다")
    void createProduct_withMultiplePinsInPersonalSale_throwsException() {
        User seller = createUser(1L, Role.USER);
        given(userRepository.findById(1L)).willReturn(Optional.of(seller));
        given(gifticonProductRepository.findByBrandAndProductName("스타벅스", "아메리카노 T")).willReturn(Optional.empty());
        given(gifticonProductRepository.save(any(GifticonProduct.class))).willAnswer(invocation -> invocation.getArgument(0));

        ProductCreateRequest request = new ProductCreateRequest(
                null,
                SaleType.PERSONAL,
                "스타벅스",
                "아메리카노 T",
                4500,
                LocalDate.now().plusDays(10),
                4000,
                null,
                List.of("1111-2222-3333", "4444-5555-6666"),
                null
        );

        assertThatThrownBy(() -> productService.createProduct(1L, request))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    @DisplayName("요청 안에 중복된 핀 번호가 있으면 실패한다")
    void createProduct_withDuplicatePinsInRequest_throwsException() {
        User seller = createUser(1L, Role.USER);
        given(userRepository.findById(1L)).willReturn(Optional.of(seller));
        given(gifticonProductRepository.findByBrandAndProductName("스타벅스", "아메리카노 T")).willReturn(Optional.empty());
        given(gifticonProductRepository.save(any(GifticonProduct.class))).willAnswer(invocation -> invocation.getArgument(0));

        ProductCreateRequest request = new ProductCreateRequest(
                null,
                SaleType.PERSONAL,
                "스타벅스",
                "아메리카노 T",
                4500,
                LocalDate.now().plusDays(10),
                4000,
                null,
                List.of("1111-2222-3333", "1111-2222-3333"),
                null
        );

        assertThatThrownBy(() -> productService.createProduct(1L, request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("중복된 핀번호입니다.");
    }

    @Test
    @DisplayName("이미 사용된 핀 번호면 실패한다")
    void createProduct_withAlreadyUsedPin_throwsException() {
        User seller = createUser(1L, Role.USER);
        given(userRepository.findById(1L)).willReturn(Optional.of(seller));
        given(gifticonProductRepository.findByBrandAndProductName("스타벅스", "아메리카노 T")).willReturn(Optional.empty());
        given(gifticonProductRepository.save(any(GifticonProduct.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(pinEncryptor.hash("1111-2222-3333")).willReturn("pin-hash");
        given(gifticonPinRepository.existsByPinHash("pin-hash")).willReturn(true);

        ProductCreateRequest request = new ProductCreateRequest(
                null,
                SaleType.PERSONAL,
                "스타벅스",
                "아메리카노 T",
                4500,
                LocalDate.now().plusDays(10),
                4000,
                "1111-2222-3333",
                null,
                null
        );

        assertThatThrownBy(() -> productService.createProduct(1L, request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("이미 사용된 핀번호입니다.");
    }

    private User createUser(Long id, Role role) {
        User seller = new User("seller@test.com", "password", "판매자", role, 0);
        ReflectionTestUtils.setField(seller, "id", id);
        return seller;
    }
}