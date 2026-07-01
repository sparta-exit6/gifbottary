package com.example.gifbottary.domain.purchase.service;

import com.example.gifbottary.common.config.CacheConfig;
import com.example.gifbottary.common.exception.ErrorCode;
import com.example.gifbottary.common.exception.ServiceException;
import com.example.gifbottary.common.util.PinEncryptor;
import com.example.gifbottary.domain.product.entity.GifticonPin;
import com.example.gifbottary.domain.product.entity.GifticonSale;
import com.example.gifbottary.domain.product.enums.PinSaleStatus;
import com.example.gifbottary.domain.product.enums.PinValidationStatus;
import com.example.gifbottary.domain.product.enums.SaleType;
import com.example.gifbottary.domain.product.repository.GifticonPinRepository;
import com.example.gifbottary.domain.product.repository.GifticonSaleRepository;
import com.example.gifbottary.domain.purchase.dto.response.PurchaseDetailResponse;
import com.example.gifbottary.domain.purchase.dto.response.PurchaseSummaryResponse;
import com.example.gifbottary.domain.purchase.entity.Purchase;
import com.example.gifbottary.domain.purchase.enums.PurchaseStatus;
import com.example.gifbottary.domain.purchase.repository.PurchaseRepository;
import com.example.gifbottary.domain.user.entity.User;
import com.example.gifbottary.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;

/**
 * 구매 생성, 구매 내역 조회, 핀 번호 노출을 담당하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final GifticonSaleRepository gifticonSaleRepository;
    private final GifticonPinRepository gifticonPinRepository;
    private final UserRepository userRepository;
    private final PinEncryptor pinEncryptor;

    /**
     * 판매글을 구매합니다.
     * 현재 단계에서는 결제 도메인이 완전히 분리되지 않았기 때문에
     * 구매 생성과 결제 완료 처리를 하나의 트랜잭션 안에서 함께 진행합니다.
     */
    @CacheEvict(cacheNames = CacheConfig.PRODUCT_SEARCH_V2_CACHE, allEntries = true)
    @Transactional
    public PurchaseDetailResponse createPurchase(Long buyerId, Long saleId) {
        User buyer = findUser(buyerId);
        GifticonSale sale = findSale(saleId);

        validateBuyerIsNotSeller(buyerId, sale);

        GifticonPin purchasedPin = findPurchasablePin(saleId);
        purchasedPin.markSold();

        Purchase purchase = Purchase.create(buyer, sale, 1);
        purchase.markPaid();
        confirmPurchaseBySaleType(purchase, sale.getSaleType());

        sale.synchronizeStockAndStatus();
        Purchase savedPurchase = purchaseRepository.save(purchase);
        return toDetailResponse(savedPurchase);
    }

    /**
     * 로그인한 사용자의 구매 목록을 최신순으로 조회합니다.
     */
    @Transactional(readOnly = true)
    public Page<PurchaseSummaryResponse> findMyPurchases(Long buyerId, Pageable pageable) {
        findUser(buyerId);
        return purchaseRepository.findAllByBuyer_IdOrderByCreatedAtDesc(buyerId, pageable)
                .map(PurchaseSummaryResponse::from);
    }

    /**
     * 구매 상세를 조회합니다.
     */
    @Transactional(readOnly = true)
    public PurchaseDetailResponse findPurchase(Long buyerId, Long purchaseId) {
        Purchase purchase = findOwnedPurchase(buyerId, purchaseId);
        return toDetailResponse(purchase);
    }

    /**
     * 플랫폼 구매의 마스킹된 핀 번호를 노출합니다.
     * 핀 번호가 노출되는 시점에 구매 확정 및 환불 불가 상태로 전이됩니다.
     */
    @Transactional
    public PurchaseDetailResponse revealPin(Long buyerId, Long purchaseId) {
        Purchase purchase = findOwnedPurchase(buyerId, purchaseId);

        if (!purchase.isMasked()) {
            return toDetailResponse(purchase);
        }

        if (purchase.getPurchaseStatus() != PurchaseStatus.PAID) {
            throw new ServiceException(ErrorCode.INVALID_PURCHASE_STATUS);
        }

        purchase.confirmPlatformPurchase();
        return toDetailResponse(purchase);
    }

    private PurchaseDetailResponse toDetailResponse(Purchase purchase) {
        String decryptedPin = resolvePinNumber(purchase);
        String exposedPin = purchase.isMasked() ? maskPin(decryptedPin) : decryptedPin;
        return PurchaseDetailResponse.from(purchase, exposedPin);
    }

    /**
     * 현재 구조에서는 Purchase가 개별 핀을 직접 참조하지 않으므로,
     * 판매글에 연결된 SOLD 핀 중 하나를 조회해 응답에 사용합니다.
     * 추후 플랫폼 다건 구매/부분 환불이 들어오면 Purchase-Pin 직접 연결을 고려해야 합니다.
     */
    private String resolvePinNumber(Purchase purchase) {
        return purchase.getSale().getPins().stream()
                .filter(pin -> pin.getPinSaleStatus() == PinSaleStatus.SOLD).min(Comparator.comparingLong(GifticonPin::getId))
                .map(GifticonPin::getEncryptedPin)
                .map(pinEncryptor::decrypt)
                .orElse(null);
    }

    /**
     * 마스킹된 핀 번호 표현입니다.
     * 예: 1111-2222-3333 -> ****-****-3333
     */
    private String maskPin(String rawPin) {
        String normalized = rawPin == null ? "" : rawPin.trim();
        if (normalized.isBlank()) {
            return null;
        }
        if (normalized.length() <= 4) {
            return "****";
        }
        return "****-****-" + normalized.substring(normalized.length() - 4);
    }

    private void validateBuyerIsNotSeller(Long buyerId, GifticonSale sale) {
        if (sale.isOwnedBy(buyerId)) {
            throw new ServiceException(ErrorCode.CONFLICT);
        }
    }

    private GifticonPin findPurchasablePin(Long saleId) {
        return gifticonPinRepository
                .findFirstBySale_IdAndPinValidationStatusAndPinSaleStatusOrderByIdAsc(
                        saleId,
                        PinValidationStatus.VALID,
                        PinSaleStatus.AVAILABLE
                )
                .orElseThrow(() -> new ServiceException(ErrorCode.PRODUCT_NOT_AVAILABLE));
    }

    private void confirmPurchaseBySaleType(Purchase purchase, SaleType saleType) {
        if (saleType == SaleType.PERSONAL) {
            purchase.confirmPersonalPurchase();
        }
    }

    private Purchase findOwnedPurchase(Long buyerId, Long purchaseId) {
        Purchase purchase = purchaseRepository.findDetailById(purchaseId)
                .orElseThrow(() -> new ServiceException(ErrorCode.PURCHASE_NOT_FOUND));

        if (!purchase.isOwner(buyerId)) {
            throw new ServiceException(ErrorCode.PURCHASE_OWNERSHIP_MISMATCH);
        }

        return purchase;
    }

    private GifticonSale findSale(Long saleId) {
        return gifticonSaleRepository.findDetailById(saleId)
                .orElseThrow(() -> new ServiceException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));
    }
}