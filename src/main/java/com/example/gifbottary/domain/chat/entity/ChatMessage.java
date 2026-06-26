package com.example.gifbottary.domain.chat.entity;

import com.example.gifbottary.common.entity.BaseEntity;
import com.example.gifbottary.domain.chat.enums.MessageType;
import com.example.gifbottary.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_message", indexes = {
    @Index(name = "idx_chat_room_id_id", columnList = "chat_room_id, id DESC")
})
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    public ChatMessage(ChatRoom chatRoom, User sender, String content) {
        this(chatRoom, sender, content, MessageType.TALK);
    }

    public ChatMessage(ChatRoom chatRoom, User sender, String content, MessageType messageType) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.content = content;
        this.messageType = messageType;
    }
}
