package com.example.gifbottary.domain.product.dto.request;

public record ProductSearchRequest(
        String keyword,
        String brand,
        Integer minPrice,
        Integer maxPrice
) {

    public boolean hasKeyword() {
        return keyword != null && !keyword.isBlank();
    }

    public boolean hasBrand() {
        return brand != null && !brand.isBlank();
    }

    public String normalizedKeyword() {
        return hasKeyword() ? keyword.trim() : null;
    }

    public String normalizedBrand() {
        return hasBrand() ? brand.trim() : null;
    }
}