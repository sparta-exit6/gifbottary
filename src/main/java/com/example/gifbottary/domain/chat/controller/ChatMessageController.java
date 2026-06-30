package com.example.gifbottary.domain.chat.controller;

import com.example.gifbottary.domain.chat.dto.request.ChatMessageSendRequest;
import com.example.gifbottary.domain.chat.dto.response.ChatMessageResponse;
import com.example.gifbottary.domain.chat.service.ChatMessageService;
import tools.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final StringRedisTemplate stringRedisTemplate;
    private final JsonMapper jsonMapper;

    @MessageMapping("/chat/message")
    public void sendMessage(Principal principal, ChatMessageSendRequest request) {
        Long senderId = Long.parseLong(principal.getName());

        // 1. 메시지 DB 저장
        ChatMessageResponse response = chatMessageService.saveMessage(senderId, request);

        // 2. 다중 서버 브로드캐스팅을 위해 Redis 채널에 메시지 발행(Publish)
        try {
            String json = jsonMapper.writeValueAsString(response);
            stringRedisTemplate.convertAndSend("chat-room:" + request.roomId(), json);
        } catch (Exception e) {
            log.error("Redis Pub/Sub 메시지 발행 실패 - Room: {}", request.roomId(), e);
        }
    }
}
