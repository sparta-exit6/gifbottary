package com.example.gifbottary.domain.chat.dto.request;

public record ChatRoomCreateRequest(
    Long saleId,
    Long buyerId // 임시용. 향후 JWT 인증 연동 시 삭제되고 Authentication에서 가져옴
) {
}
