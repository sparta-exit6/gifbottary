package com.example.gifbottary.domain.chat.dto.request;

public record ChatMessageSendRequest(
    Long roomId,
    String content
) {
}
