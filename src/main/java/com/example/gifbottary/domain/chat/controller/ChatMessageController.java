package com.example.gifbottary.domain.chat.controller;

import com.example.gifbottary.domain.chat.dto.request.ChatMessageSendRequest;
import com.example.gifbottary.domain.chat.dto.response.ChatMessageResponse;
import com.example.gifbottary.domain.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageSendRequest request) {
        // 1. 메시지 DB 저장
        ChatMessageResponse response = chatMessageService.saveMessage(request);
        
        // 2. 해당 채팅방 구독자들에게 메시지 브로드캐스트
        messagingTemplate.convertAndSend("/sub/chat/" + request.roomId(), response);
    }
}
