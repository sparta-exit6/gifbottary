package com.example.gifbottary.domain.chat.service;

import com.example.gifbottary.domain.chat.repository.ChatMemberRepository;
import com.example.gifbottary.domain.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMemberService {

    private final ChatMemberRepository chatMemberRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public void updateLastReadMessageId(Long roomId, Long userId) {
        Long maxId = chatMessageRepository.findMaxMessageIdByRoomId(roomId);
        if (maxId != null) {
            chatMemberRepository.findByChatRoomIdAndUserId(roomId, userId)
                    .ifPresent(member -> member.updateLastReadMessageId(maxId));
        }
    }
}
