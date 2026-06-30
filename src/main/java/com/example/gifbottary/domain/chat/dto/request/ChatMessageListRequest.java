package com.example.gifbottary.domain.chat.dto.request;

public record ChatMessageListRequest(
        Long lastMessageId,
        Integer size
) {
    public ChatMessageListRequest {
        if (size == null || size <= 0) {
            size = 20;
        }
    }
}
