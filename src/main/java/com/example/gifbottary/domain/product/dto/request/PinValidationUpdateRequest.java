package com.example.gifbottary.domain.product.dto.request;

import com.example.gifbottary.domain.product.enums.PinValidationStatus;
import jakarta.validation.constraints.NotNull;

public record PinValidationUpdateRequest(
        @NotNull PinValidationStatus pinValidationStatus
) {
}
