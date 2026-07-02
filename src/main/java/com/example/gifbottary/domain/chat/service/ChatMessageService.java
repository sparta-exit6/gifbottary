package com.example.gifbottary.domain.chat.service;

import com.example.gifbottary.common.exception.ErrorCode;
import com.example.gifbottary.common.exception.ServiceException;
import com.example.gifbottary.domain.chat.dto.request.ChatMessageListRequest;
import com.example.gifbottary.domain.chat.dto.request.ChatMessageSendRequest;
import com.example.gifbottary.domain.chat.dto.request.ChatMissedMessageRequest;
import com.example.gifbottary.domain.chat.dto.response.ChatMessageResponse;
import com.example.gifbottary.domain.chat.entity.ChatMessage;
import com.example.gifbottary.domain.chat.entity.ChatRoom;
import com.example.gifbottary.domain.chat.enums.MessageType;
import com.example.gifbottary.domain.chat.repository.ChatMemberRepository;
import com.example.gifbottary.domain.chat.repository.ChatMessageRepository;
import com.example.gifbottary.domain.chat.repository.ChatRoomRepository;
import com.example.gifbottary.domain.user.entity.User;
import com.example.gifbottary.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final UserRepository userRepository;

    public void validateChatMember(Long roomId, Long userId) {
        if (!chatMemberRepository.existsByChatRoomIdAndUserId(roomId, userId)) {
            throw new ServiceException(ErrorCode.CHATROOM_ACCESS_DENIED);
        }
    }

    @Transactional
    public ChatMessageResponse saveMessage(Long senderId, ChatMessageSendRequest request) {
        validateChatMember(request.roomId(), senderId);

        ChatRoom chatRoom = chatRoomRepository.findById(request.roomId())
                .orElseThrow(() -> new ServiceException(ErrorCode.CHATROOM_NOT_FOUND));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        ChatMessage message = new ChatMessage(chatRoom, sender, request.content(), MessageType.TALK);
        ChatMessage savedMessage = chatMessageRepository.save(message);

        chatRoom.updateLastMessageAt(LocalDateTime.now());

        return ChatMessageResponse.from(savedMessage);
    }

    @Transactional
    public ChatMessageResponse saveSystemMessage(Long roomId, Long userId, String content) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ServiceException(ErrorCode.CHATROOM_NOT_FOUND));

        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        ChatMessage message = new ChatMessage(chatRoom, sender, content, MessageType.SYSTEM);
        ChatMessage savedMessage = chatMessageRepository.save(message);

        return ChatMessageResponse.from(savedMessage);
    }

    public List<ChatMessageResponse> getMessages(Long roomId, ChatMessageListRequest request, Long userId) {
        validateChatMember(roomId, userId);
        Pageable pageable = PageRequest.of(0, request.size());
        List<ChatMessage> messages = chatMessageRepository.findMessages(roomId, request.lastMessageId(), pageable);

        return messages.stream()
                .map(ChatMessageResponse::from)
                .collect(Collectors.toList());
    }

    public List<ChatMessageResponse> getMissedMessages(Long roomId, ChatMissedMessageRequest request, Long userId) {
        validateChatMember(roomId, userId);
        List<ChatMessage> messages = chatMessageRepository.findMissedMessages(roomId, request.lastMessageId());

        return messages.stream()
                .map(ChatMessageResponse::from)
                .collect(Collectors.toList());
    }
}
