package com.example.gifbottary.domain.chat.dto.request;

import jakarta.validation.constraints.NotNull;

public record ChatMessageListRequest(
        @NotNull
        Long roomId,
        Long lastMessageId,
        Integer size
) {
    public ChatMessageListRequest {
        if (size == null || size <= 0) {
            size = 20;
        }
    }
}
