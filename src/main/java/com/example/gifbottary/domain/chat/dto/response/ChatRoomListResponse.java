package com.example.gifbottary.domain.chat.dto.response;

import com.example.gifbottary.domain.product.enums.SaleStatus;
import java.time.LocalDateTime;

public record ChatRoomListResponse(
    Long roomId,
    Long saleId,
    String otherUserName,
    String productName,
    Integer salePrice,
    SaleStatus saleStatus,
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
