package com.example.gifbottary.domain.product.dto.response;

import com.example.gifbottary.domain.product.entity.GifticonPin;

import java.time.LocalDateTime;

/**
 * 판매글에 포함된 개별 핀의 상태 조회 응답입니다.
 */
public record PinDetailResponse(
        Long pinId,
        String pinValidationStatus,
        String pinSaleStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PinDetailResponse from(GifticonPin pin) {
        return new PinDetailResponse(
                pin.getId(),
                pin.getPinValidationStatus().name(),
                pin.getPinSaleStatus().name(),
                pin.getCreatedAt(),
                pin.getUpdatedAt()
        );
    }
}
