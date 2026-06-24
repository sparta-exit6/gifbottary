package com.example.gifbottary.domain.chat.entity;

import com.example.gifbottary.common.entity.BaseEntity;
import com.example.gifbottary.domain.chat.enums.ChatRoomStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_room")
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sale_id", nullable = false)
    private Long saleId;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRoomStatus chatRoomStatus;

    @Column(name = "unread_count", nullable = false)
    private int unreadCount;

    public ChatRoom(Long saleId, Long buyerId, Long sellerId) {
        this.saleId = saleId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.chatRoomStatus = ChatRoomStatus.OPEN;
        this.unreadCount = 0;
    }

    public void updateStatus(ChatRoomStatus chatRoomStatus) {
        this.chatRoomStatus = chatRoomStatus;
    }
}
