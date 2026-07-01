package com.example.gifbottary.domain.product.repository;

import com.example.gifbottary.domain.product.entity.GifticonPin;
import com.example.gifbottary.domain.product.enums.PinSaleStatus;
import com.example.gifbottary.domain.product.enums.PinValidationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface GifticonPinRepository extends JpaRepository<GifticonPin, Long> {

    Optional<GifticonPin> findByIdAndSale_Id(Long pinId, Long saleId);

    List<GifticonPin> findAllBySale_IdOrderByIdAsc(Long saleId);

    boolean existsBySale_IdAndPinSaleStatus(Long saleId, PinSaleStatus pinSaleStatus);

    boolean existsByPinHash(String pinHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<GifticonPin> findFirstBySale_IdAndPinValidationStatusAndPinSaleStatusOrderByIdAsc(
            Long saleId,
            PinValidationStatus pinValidationStatus,
            PinSaleStatus pinSaleStatus
    );

    /**
     * 판매 가능한 핀을 구매 수량만큼 비관적 락으로 조회합니다.
     * 플랫폼 다건 구매 시 같은 핀을 동시에 선점하는 문제를 막기 위한 메서드입니다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<GifticonPin> findBySale_IdAndPinValidationStatusAndPinSaleStatusOrderByIdAsc(
            Long saleId,
            PinValidationStatus pinValidationStatus,
            PinSaleStatus pinSaleStatus,
            Pageable pageable
    );
}

