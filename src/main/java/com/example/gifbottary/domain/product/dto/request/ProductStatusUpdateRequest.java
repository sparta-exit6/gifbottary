package com.example.gifbottary.domain.product.dto.request;

import com.example.gifbottary.domain.product.enums.SaleStatus;
import jakarta.validation.constraints.NotNull;

public record ProductStatusUpdateRequest(
        @NotNull SaleStatus saleStatus
) {
}
