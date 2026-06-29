package com.example.gifbottary.domain.chat.repository;

import com.example.gifbottary.domain.chat.entity.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {

    Optional<ChatMember> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);

    void deleteByChatRoomIdAndUserId(Long chatRoomId, Long userId);
}
