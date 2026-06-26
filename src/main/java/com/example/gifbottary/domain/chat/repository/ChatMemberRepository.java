package com.example.gifbottary.domain.chat.repository;

import com.example.gifbottary.domain.chat.entity.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {

    void deleteByChatRoomIdAndUserId(Long chatRoomId, Long userId);
}
