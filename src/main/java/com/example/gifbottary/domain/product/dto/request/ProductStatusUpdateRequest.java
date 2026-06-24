package com.example.gifbottary.domain.product.dto.request;

import com.example.gifbottary.domain.product.enums.SaleStatus;

public record ProductStatusUpdateRequest(
        SaleStatus saleStatus
) {
}
