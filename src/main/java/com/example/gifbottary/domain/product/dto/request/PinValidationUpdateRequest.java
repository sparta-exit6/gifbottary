package com.example.gifbottary.domain.product.dto.request;

import com.example.gifbottary.domain.product.enums.PinValidationStatus;

public record PinValidationUpdateRequest(
        PinValidationStatus pinValidationStatus
) {
}
