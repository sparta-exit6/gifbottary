package com.example.gifbottary.domain.product.dto.response;

import com.example.gifbottary.domain.product.entity.GifticonPin;
import com.example.gifbottary.domain.product.entity.GifticonSale;
import com.example.gifbottary.domain.product.enums.PinSaleStatus;
import com.example.gifbottary.domain.product.enums.PinValidationStatus;

import java.util.List;

/**
 * [관리자] 기프티콘 핀번호 검수 현황 및 통계 조회 응답 DTO입니다.
 * 특정 판매글(Sale)에 속한 전체 핀번호들의 검수 상태별 개수 통계와
 * 개별 핀번호의 상세 내역 리스트를 함께 반환합니다.
 */
public record ProductPinValidationResponse(
        Long saleId,
        String saleType,
        String saleStatus,
        int totalPinCount,
        int pendingCount,
        int validCount,
        int invalidCount,
        int soldCount,
        List<PinDetailResponse> pins
) {
    public static ProductPinValidationResponse from(GifticonSale sale, List<GifticonPin> pins) {
        int pendingCount = (int) pins.stream()
                .filter(pin -> pin.getPinValidationStatus() == PinValidationStatus.PENDING)
                .count();
        int validCount = (int) pins.stream()
                .filter(pin -> pin.getPinValidationStatus() == PinValidationStatus.VALID)
                .count();
        int invalidCount = (int) pins.stream()
                .filter(pin -> pin.getPinValidationStatus() == PinValidationStatus.INVALID)
                .count();
        int soldCount = (int) pins.stream()
                .filter(pin -> pin.getPinSaleStatus() == PinSaleStatus.SOLD)
                .count();

        return new ProductPinValidationResponse(
                sale.getId(),
                sale.getSaleType().name(),
                sale.getSaleStatus().name(),
                pins.size(),
                pendingCount,
                validCount,
                invalidCount,
                soldCount,
                pins.stream().map(PinDetailResponse::from).toList()
        );
    }
}
