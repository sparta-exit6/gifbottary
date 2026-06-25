package com.example.gifbottary.domain.chat.dto.response;

import com.example.gifbottary.domain.chat.entity.ChatMessage;

import java.time.LocalDateTime;

public record ChatMessageResponse(
    Long messageId,
    Long senderId,
    String senderName,
    String content,
    LocalDateTime createdAt
) {
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
            message.getId(),
            message.getSender().getId(),
            message.getSender().getName(),
            message.getContent(),
            message.getCreatedAt()
        );
    }
}
