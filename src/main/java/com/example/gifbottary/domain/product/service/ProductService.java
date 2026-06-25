package com.example.gifbottary.domain.product.service;

import com.example.gifbottary.common.exception.ErrorCode;
import com.example.gifbottary.common.exception.ServiceException;
import com.example.gifbottary.common.util.PinEncryptor;
import com.example.gifbottary.domain.product.dto.request.*;
import com.example.gifbottary.domain.product.dto.response.*;
import com.example.gifbottary.domain.product.entity.GifticonPin;
import com.example.gifbottary.domain.product.entity.GifticonProduct;
import com.example.gifbottary.domain.product.entity.GifticonSale;
import com.example.gifbottary.domain.product.enums.PinSaleStatus;
import com.example.gifbottary.domain.product.enums.PinValidationStatus;
import com.example.gifbottary.domain.product.enums.SaleStatus;
import com.example.gifbottary.domain.product.enums.SaleType;
import com.example.gifbottary.domain.product.repositroy.GifticonPinRepository;
import com.example.gifbottary.domain.product.repositroy.GifticonProductRepository;
import com.example.gifbottary.domain.product.repositroy.GifticonSaleRepository;
import com.example.gifbottary.domain.product.repositroy.GifticonSaleSpecification;
import com.example.gifbottary.domain.user.entity.User;
import com.example.gifbottary.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 상품/판매글 CRUD와 판매자 상품 조회를 담당하는 서비스 클래스입니다.
 * 현재 팀 구조에서는 구현체가 하나뿐이므로 인터페이스 없이 단일 클래스로 관리합니다.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final GifticonProductRepository gifticonProductRepository;
    private final GifticonSaleRepository gifticonSaleRepository;
    private final GifticonPinRepository gifticonPinRepository;
    private final UserRepository userRepository;
    private final PinEncryptor pinEncryptor;

    /**
     * 판매글을 등록합니다.
     * 기존 상품 선택과 상품 정보 직접 입력 두 방식을 모두 지원합니다.
     */
    @Transactional
    public ProductCreateResponse createProduct(Long sellerId, ProductCreateRequest request) {
        User seller = findUser(sellerId);
        validateCreateRequest(request, seller);
        GifticonProduct product = resolveProduct(request);

        GifticonSale sale = new GifticonSale(seller, product, request.saleType(), request.salePrice(), request.expireAt());
        appendPins(sale, extractPinNumbers(request.pinNumber(), request.pinNumbers()), request.saleType());
        sale.synchronizeStockAndStatus();

        GifticonSale savedSale = gifticonSaleRepository.save(sale);
        return ProductCreateResponse.from(savedSale);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse findProduct(Long saleId) {
        return toDetailResponse(findSale(saleId));
    }

    /**
     * 공개 판매글 목록을 조회합니다.
     * 인증 없이도 조회 가능하며, 공개 가능한 판매글만 반환합니다.
     */
    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> findProducts(ProductSearchRequest request, Pageable pageable) {
        return gifticonSaleRepository.findAll(GifticonSaleSpecification.publicSearch(request), pageable)
                .map(ProductSummaryResponse::from);
    }

    /**
     * 판매 가격, 유효기간, 이미지, 핀 목록을 수정합니다.
     */
    @Transactional
    public ProductDetailResponse updateProduct(Long sellerId, Long saleId, ProductUpdateRequest request) {
        GifticonSale sale = findOwnedSale(sellerId, saleId);
        if (sale.getSaleStatus() == SaleStatus.CANCELLED) {
            throw new ServiceException(ErrorCode.INVALID_SALE_STATUS);
        }
        sale.updateSaleInfo(request.salePrice());

        if (request.imageUrl() != null && !request.imageUrl().isBlank()) {
            sale.getProduct().updateImageUrl(request.imageUrl().trim());
        }

        if (request.pinNumbers() != null && !request.pinNumbers().isEmpty()) {
            appendPins(sale, request.pinNumbers(), sale.getSaleType());
        }

        sale.synchronizeStockAndStatus();
        return toDetailResponse(sale);
    }

    @Transactional(readOnly = true)
    public ProductPinValidationResponse findPinValidation(Long sellerId, Long saleId) {
        GifticonSale sale = findOwnedSale(sellerId, saleId);
        List<GifticonPin> pins = gifticonPinRepository.findAllBySale_IdOrderByIdAsc(sale.getId());
        return ProductPinValidationResponse.from(sale, pins);
    }

    /**
     * 판매글 상태를 수동으로 변경합니다.
     */
    @Transactional
    public ProductDetailResponse updateSaleStatus(Long sellerId, Long saleId, ProductStatusUpdateRequest request) {
        GifticonSale sale = findOwnedSale(sellerId, saleId);

        try {
            sale.changeSaleStatus(request.saleStatus());
        } catch (IllegalStateException exception) {
            throw new ServiceException(ErrorCode.INVALID_SALE_STATUS);
        }

        return toDetailResponse(sale);
    }

    /**
     * 목데이터 기반 핀 검수 결과를 반영합니다.
     */
    @Transactional
    public ProductDetailResponse updatePinValidationStatus(Long sellerId, Long saleId, Long pinId, PinValidationUpdateRequest request) {
        GifticonSale sale = findOwnedSale(sellerId, saleId);
        if (sale.getSaleStatus() == SaleStatus.CANCELLED) {
            throw new ServiceException(ErrorCode.INVALID_SALE_STATUS);
        }
        GifticonPin pin = gifticonPinRepository.findByIdAndSale_Id(pinId, saleId).orElseThrow(
                () -> new ServiceException(ErrorCode.PIN_NOT_FOUND));

        if (request.pinValidationStatus() == PinValidationStatus.VALID) {
            pin.validatePin();
        } else if (request.pinValidationStatus() == PinValidationStatus.INVALID) {
            pin.invalidatePin();
        } else {
            throw new ServiceException(ErrorCode.INVALID_PIN_STATUS);
        }

        sale.synchronizeStockAndStatus();
        return toDetailResponse(sale);
    }

    /**
     * 이미 판매된 핀이 없다면 판매글을 취소 상태로 전환합니다.
     */
    @Transactional
    public void removeProduct(Long sellerId, Long saleId) {
        GifticonSale sale = findOwnedSale(sellerId, saleId);

        if (gifticonPinRepository.existsBySale_IdAndPinSaleStatus(saleId, PinSaleStatus.SOLD)) {
            throw new ServiceException(ErrorCode.PRODUCT_ALREADY_SOLD);
        }

        sale.changeSaleStatus(SaleStatus.CANCELLED);
    }

    @Transactional(readOnly = true)
    public Page<MyProductSummaryResponse> findMyProducts(Long sellerId, ProductSearchRequest request, Pageable pageable) {
        findUser(sellerId);
        return gifticonSaleRepository.findAll(GifticonSaleSpecification.sellerSearch(sellerId, request), pageable)
                .map(MyProductSummaryResponse::from);
    }

    private void validateCreateRequest(ProductCreateRequest request, User seller) {
        if (request.expireAt() == null || request.expireAt().isBefore(LocalDate.now())) {
            throw new ServiceException(ErrorCode.INVALID_EXPIRE_AT);
        }
        if (request.salePrice() == null || request.salePrice() < 0) {
            throw new ServiceException(ErrorCode.INVALID_PRICE);
        }
        // 플랫폼 상품 등록은 관리자만 가능합니다.
        if (request.saleType() == SaleType.PLATFORM && !isAdmin(seller)) {
            throw new ServiceException(ErrorCode.PLATFORM_SALE_ADMIN_ONLY);
        }
        // 개인 판매자는 상품 ID를 직접 선택하지 않고 상품 정보를 직접 입력합니다.
        if (request.saleType() == SaleType.PERSONAL && request.productId() != null) {
            throw new ServiceException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (request.productId() == null) {
            if (isBlank(request.brand()) || isBlank(request.productName()) || request.faceValue() == null) {
                throw new ServiceException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }
    }

    /**
     * 판매 등록 시 기존 상품을 선택하거나 상품 정보를 직접 입력하는 두 가지 방식을 지원합니다.
     */
    private GifticonProduct resolveProduct(ProductCreateRequest request) {
        if (request.productId() != null) {
            return gifticonProductRepository.findById(request.productId())
                    .orElseThrow(() -> new ServiceException(ErrorCode.PRODUCT_NOT_FOUND));
        }

        return gifticonProductRepository.findByBrandAndProductName(request.brand().trim(), request.productName().trim())
                .orElseGet(() -> gifticonProductRepository.save(
                        new GifticonProduct(
                                request.brand().trim(),
                                request.productName().trim(),
                                request.faceValue(),
                                request.imageUrl()
                        )
                ));
    }

    /**
     * 핀 번호는 정규화 후 암호화하여 개별 핀 엔티티로 추가합니다.
     */
    private void appendPins(GifticonSale sale, List<String> rawPins, SaleType saleType) {
        List<String> normalizedPins = normalizePinNumbers(rawPins);
        validatePinCountBySaleType(sale, normalizedPins, saleType);

        for (String rawPin : normalizedPins) {
            String encryptedPin = pinEncryptor.encrypt(rawPin);
            if (gifticonPinRepository.existsByEncryptedPin(encryptedPin)) {
                throw new ServiceException(ErrorCode.PIN_VALIDATION_FAILED, "이미 사용된 핀번호입니다.");
            }
            GifticonPin gifticonPin = new GifticonPin(encryptedPin);
            sale.addPin(gifticonPin);
        }
    }

    /**
     * 개인 판매는 항상 1핀만 허용하고, 플랫폼 판매만 다건 핀을 허용합니다.
     */
    private void validatePinCountBySaleType(GifticonSale sale, List<String> normalizedPins, SaleType saleType) {
        if (normalizedPins.isEmpty()) {
            throw new ServiceException(ErrorCode.INVALID_PIN_COUNT);
        }

        int totalPinCount = sale.getPins().size() + normalizedPins.size();
        if (saleType == SaleType.PERSONAL && totalPinCount != 1) {
            throw new ServiceException(ErrorCode.INVALID_PIN_COUNT);
        }
    }

    private List<String> normalizePinNumbers(List<String> rawPins) {
        LinkedHashSet<String> distinctPins = new LinkedHashSet<>();
        for (String rawPin : rawPins) {
            if (rawPin == null || rawPin.isBlank()) {
                throw new ServiceException(ErrorCode.INVALID_PIN_INPUT, "유효하지 않은 핀번호입니다.");
            }
            String normalizedPin = rawPin.trim();
            if (!distinctPins.add(normalizedPin)) {
                throw new ServiceException(ErrorCode.PIN_VALIDATION_FAILED, "중복된 핀번호입니다.");
            }
        }
        return new ArrayList<>(distinctPins);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isAdmin(User seller) {
        return seller.getRole() != null && "ADMIN".equalsIgnoreCase(seller.getRole().trim());
    }

    private List<String> extractPinNumbers(String pinNumber, List<String> pinNumbers) {
        List<String> rawPins = new ArrayList<>();
        if (pinNumber != null && !pinNumber.isBlank()) {
            rawPins.add(pinNumber);
        }
        if (pinNumbers != null && !pinNumbers.isEmpty()) {
            rawPins.addAll(pinNumbers);
        }
        return rawPins;
    }

    private ProductDetailResponse toDetailResponse(GifticonSale sale) {
        List<PinDetailResponse> pinResponses = gifticonPinRepository.findAllBySale_IdOrderByIdAsc(sale.getId())
                .stream()
                .map(PinDetailResponse::from)
                .toList();
        return ProductDetailResponse.from(sale, pinResponses);
    }

    private GifticonSale findSale(Long saleId) {
        return gifticonSaleRepository.findDetailById(saleId)
                .orElseThrow(() -> new ServiceException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private GifticonSale findOwnedSale(Long sellerId, Long saleId) {
        GifticonSale sale = findSale(saleId);
        if (!sale.isOwnedBy(sellerId)) {
            throw new ServiceException(ErrorCode.PRODUCT_OWNERSHIP_MISMATCH);
        }
        return sale;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new ServiceException(ErrorCode.USER_NOT_FOUND));
    }
}
