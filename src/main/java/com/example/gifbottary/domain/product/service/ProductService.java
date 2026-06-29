package com.example.gifbottary.domain.product.service;

import com.example.gifbottary.common.config.CacheConfig;
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
import com.example.gifbottary.domain.product.repository.GifticonPinRepository;
import com.example.gifbottary.domain.product.repository.GifticonProductRepository;
import com.example.gifbottary.domain.product.repository.GifticonSaleRepository;
import com.example.gifbottary.domain.user.entity.Role;
import com.example.gifbottary.domain.user.entity.User;
import com.example.gifbottary.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 상품/판매글 CRUD와 공개 목록 조회, 판매자 목록 조회를 담당하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final GifticonProductRepository gifticonProductRepository;
    private final GifticonSaleRepository gifticonSaleRepository;
    private final GifticonPinRepository gifticonPinRepository;
    private final UserRepository userRepository;
    private final PinEncryptor pinEncryptor;

    @CacheEvict(cacheNames = "productSearchV2", allEntries = true)
    @Transactional
    public ProductCreateResponse createProduct(Long sellerId, ProductCreateRequest request) {
        User seller = findUser(sellerId);
        validateCreateRequest(request, seller);
        GifticonProduct product = resolveProduct(request);

        GifticonSale sale = new GifticonSale(
                seller,
                product,
                request.saleType(),
                request.salePrice(),
                request.expireAt()
        );

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
     * v1 상품 검색 API
     */
    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> searchProductsV1(ProductSearchRequest request, Pageable pageable) {
        return gifticonSaleRepository.searchProducts(request, pageable);
    }

    /**
     * v2 상품 검색 API
     * 동일한 검색 조건에 대해서는 Caffeine 로컬 캐시를 우선 사용합니다.
     * 캐시 key에는 검색 조건과 페이지 정보를 모두 포함해서
     * 서로 다른 검색 요청이 같은 캐시를 공유하지 않도록 합니다.
     */
    @Cacheable(
            cacheNames = CacheConfig.PRODUCT_SEARCH_V2_CACHE,  // 캐시 그룹 이름
            key = "'keyword:' + (#request.normalizedKeyword() == null ? '' : #request.normalizedKeyword()) + " +
                    "':brand:' + (#request.normalizedBrand() == null ? '' : #request.normalizedBrand()) + " +
                    "':minPrice:' + (#request.minPrice() == null ? '' : #request.minPrice()) + " +
                    "':maxPrice:' + (#request.maxPrice() == null ? '' : #request.maxPrice()) + " +
                    "':page:' + #pageable.pageNumber + " +
                    "':size:' + #pageable.pageSize"
    )
    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> searchProductsV2(ProductSearchRequest request, Pageable pageable) {
        return gifticonSaleRepository.searchProducts(request, pageable);
    }

    @CacheEvict(cacheNames = CacheConfig.PRODUCT_SEARCH_V2_CACHE, allEntries = true)
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

    @CacheEvict(cacheNames = CacheConfig.PRODUCT_SEARCH_V2_CACHE, allEntries = true)
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

    @CacheEvict(cacheNames = CacheConfig.PRODUCT_SEARCH_V2_CACHE, allEntries = true)
    @Transactional
    public ProductDetailResponse updatePinValidationStatus(Long sellerId, Long saleId, Long pinId, PinValidationUpdateRequest request) {
        GifticonSale sale = findOwnedSale(sellerId, saleId);
        if (sale.getSaleStatus() == SaleStatus.CANCELLED) {
            throw new ServiceException(ErrorCode.INVALID_SALE_STATUS);
        }

        GifticonPin pin = gifticonPinRepository.findByIdAndSale_Id(pinId, saleId)
                .orElseThrow(() -> new ServiceException(ErrorCode.PIN_NOT_FOUND));

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

    @CacheEvict(cacheNames = CacheConfig.PRODUCT_SEARCH_V2_CACHE, allEntries = true)
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
        return gifticonSaleRepository.searchMyProducts(sellerId, request, pageable);
    }

    private void validateCreateRequest(ProductCreateRequest request, User seller) {
        if (request.expireAt() == null || request.expireAt().isBefore(LocalDate.now())) {
            throw new ServiceException(ErrorCode.INVALID_EXPIRE_AT);
        }
        if (request.salePrice() == null || request.salePrice() < 0) {
            throw new ServiceException(ErrorCode.INVALID_PRICE);
        }
        if (request.saleType() == SaleType.PLATFORM && !isAdmin(seller)) {
            throw new ServiceException(ErrorCode.PLATFORM_SALE_ADMIN_ONLY);
        }
        if (request.saleType() == SaleType.PERSONAL && request.productId() != null) {
            throw new ServiceException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (request.productId() == null) {
            if (isBlank(request.brand()) || isBlank(request.productName()) || request.faceValue() == null) {
                throw new ServiceException(ErrorCode.PRODUCT_NOT_FOUND);
            }
        }
    }

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
     * 핀번호는 복호화 가능한 암호문으로 저장하고,
     * 중복 여부는 별도의 해시값으로 검사합니다.
     */
    private void appendPins(GifticonSale sale, List<String> rawPins, SaleType saleType) {
        List<String> normalizedPins = normalizePinNumbers(rawPins);
        validatePinCountBySaleType(sale, normalizedPins, saleType);

        for (String rawPin : normalizedPins) {
            String pinHash = pinEncryptor.hash(rawPin);
            if (gifticonPinRepository.existsByPinHash(pinHash)) {
                throw new ServiceException(ErrorCode.PIN_VALIDATION_FAILED, "이미 사용된 핀번호입니다.");
            }

            String encryptedPin = pinEncryptor.encrypt(rawPin);
            GifticonPin gifticonPin = new GifticonPin(encryptedPin, pinHash);
            sale.addPin(gifticonPin);
        }
    }

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
        return seller.getRole() == Role.ADMIN;
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
        return userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));
    }
}
