package com.example.gifbottary.domain.chat.dto.request;

import jakarta.validation.constraints.NotNull;

public record ChatMissedMessageRequest(
        @NotNull
        Long roomId,
        Long lastMessageId
) {
}
