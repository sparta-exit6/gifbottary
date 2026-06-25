package com.example.gifbottary.domain.chat.repository;

import com.example.gifbottary.domain.chat.dto.response.ChatRoomListResponse;
import com.example.gifbottary.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findBySaleIdAndBuyerId(Long saleId, Long buyerId);

    @Query("""
        SELECT new com.example.gifbottary.domain.chat.dto.response.ChatRoomListResponse(
            cr.id,
            CASE WHEN cr.buyer.id = :userId THEN s.seller.name ELSE cr.buyer.name END,
            p.productName,
            (SELECT msg.content FROM ChatMessage msg WHERE msg.chatRoom = cr ORDER BY msg.id DESC LIMIT 1),
            cr.lastMessageAt,
            (SELECT COUNT(msg) FROM ChatMessage msg WHERE msg.chatRoom = cr AND (cm.lastReadMessageId IS NULL OR msg.id > cm.lastReadMessageId))
        )
        FROM ChatRoom cr
        JOIN ChatMember cm ON cm.chatRoom = cr AND cm.user.id = :userId
        JOIN cr.sale s
        JOIN s.product p
        ORDER BY cr.lastMessageAt DESC
    """)
    List<ChatRoomListResponse> findRoomListByUserId(@Param("userId") Long userId);
}
