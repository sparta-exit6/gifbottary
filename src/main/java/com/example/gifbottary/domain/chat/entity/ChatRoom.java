package com.example.gifbottary.domain.chat.entity;

import com.example.gifbottary.common.entity.BaseEntity;
import com.example.gifbottary.domain.chat.enums.ChatRoomStatus;
import com.example.gifbottary.domain.product.entity.GifticonSale;
import com.example.gifbottary.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_room", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"sale_id", "buyer_id"})
})
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private GifticonSale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRoomStatus chatRoomStatus;

    private LocalDateTime lastMessageAt;

    public ChatRoom(GifticonSale sale, User buyer) {
        this.sale = sale;
        this.buyer = buyer;
        this.chatRoomStatus = ChatRoomStatus.OPEN;
        this.lastMessageAt = LocalDateTime.now();
    }

    public void updateStatus(ChatRoomStatus chatRoomStatus) {
        this.chatRoomStatus = chatRoomStatus;
    }

    public void updateLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }
}
