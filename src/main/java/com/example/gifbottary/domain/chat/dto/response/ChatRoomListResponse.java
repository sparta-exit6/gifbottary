package com.example.gifbottary.domain.chat.dto.response;

import java.time.LocalDateTime;

public record ChatRoomListResponse(
    Long roomId,
    String otherUserName,
    String productName,
    String lastMessageContent,
    LocalDateTime lastMessageCreatedAt,
    long unreadCount
) {
    public ChatRoomListResponse {
        if (lastMessageContent == null) {
            lastMessageContent = "";
        }
    }
}
