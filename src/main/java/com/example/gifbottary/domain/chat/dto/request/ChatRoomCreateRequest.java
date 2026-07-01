package com.example.gifbottary.domain.chat.dto.request;

import jakarta.validation.constraints.NotNull;

public record ChatRoomCreateRequest(
        @NotNull
        Long saleId
) {
}
