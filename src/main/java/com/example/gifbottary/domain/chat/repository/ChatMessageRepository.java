package com.example.gifbottary.domain.chat.repository;

import com.example.gifbottary.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m JOIN FETCH m.sender " +
           "WHERE m.chatRoom.id = :roomId " +
           "AND (:lastMessageId IS NULL OR m.id < :lastMessageId) " +
           "ORDER BY m.id DESC")
    List<ChatMessage> findMessages(@Param("roomId") Long roomId, 
                                   @Param("lastMessageId") Long lastMessageId, 
                                   Pageable pageable);

    @Query("SELECT m FROM ChatMessage m JOIN FETCH m.sender " +
           "WHERE m.chatRoom.id = :roomId " +
           "AND (:lastMessageId IS NULL OR m.id > :lastMessageId) " +
           "ORDER BY m.id ASC")
    List<ChatMessage> findMissedMessages(@Param("roomId") Long roomId, 
                                         @Param("lastMessageId") Long lastMessageId);
}
