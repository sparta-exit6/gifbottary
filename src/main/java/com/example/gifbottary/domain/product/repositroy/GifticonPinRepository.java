package com.example.gifbottary.domain.product.repositroy;

import com.example.gifbottary.domain.product.entity.GifticonPin;
import com.example.gifbottary.domain.product.enums.PinSaleStatus;
import com.example.gifbottary.domain.product.enums.PinValidationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface GifticonPinRepository extends JpaRepository<GifticonPin, Long> {

    Optional<GifticonPin> findByIdAndSale_Id(Long pinId, Long saleId);

    List<GifticonPin> findAllBySale_IdOrderByIdAsc(Long saleId);

    boolean existsBySale_IdAndPinSaleStatus(Long saleId, PinSaleStatus pinSaleStatus);

    boolean existsByEncryptedPin(String encryptedPin);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<GifticonPin> findFirstBySale_IdAndPinValidationStatusAndPinSaleStatusOrderByIdAsc(
            Long saleId,
            PinValidationStatus pinValidationStatus,
            PinSaleStatus pinSaleStatus
    );
}
