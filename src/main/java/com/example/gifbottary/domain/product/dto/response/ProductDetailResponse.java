package com.example.gifbottary.domain.product.dto.response;

import com.example.gifbottary.domain.product.entity.GifticonSale;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 판매글 단건 상세 응답 DTO입니다.
 */
public record ProductDetailResponse(
	        Long saleId,
	        Long productId,
	        Long sellerId,
	        String sellerName,
	        String saleType,
        String brand,
        String productName,
        LocalDate expireAt,
        Integer salePrice,
        Integer stock,
        String saleStatus,
        String imageUrl,
        List<PinDetailResponse> pins,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductDetailResponse from(GifticonSale sale, List<PinDetailResponse> pins) {
        return new ProductDetailResponse(
	                sale.getId(),
	                sale.getProduct().getId(),
	                sale.getSeller().getId(),
	                sale.getSeller().getName(),
	                sale.getSaleType().name(),
                sale.getProduct().getBrand(),
                sale.getProduct().getProductName(),
                sale.getExpireAt(),
                sale.getSalePrice(),
                sale.getStock(),
                sale.getSaleStatus().name(),
                sale.getProduct().getImageUrl(),
                pins,
                sale.getCreatedAt(),
                sale.getUpdatedAt()
        );
    }
}
