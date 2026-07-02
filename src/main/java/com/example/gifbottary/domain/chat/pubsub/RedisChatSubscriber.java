package com.example.gifbottary.domain.chat.pubsub;

import com.example.gifbottary.domain.chat.dto.response.ChatMessageResponse;
import tools.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Redis Pub/Sub 채널로부터 메시지를 수신하여
 * 현재 서버 인스턴스에 접속된 WebSocket STOMP 구독자들에게 메시지를 전송하는 Subscriber 클래스입니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatSubscriber implements MessageListener {

    private static final String CHANNEL_PREFIX = "chat-room:";
    private final JsonMapper jsonMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
            String body = new String(message.getBody(), StandardCharsets.UTF_8);

            if (channel.startsWith(CHANNEL_PREFIX)) {
                String roomId = channel.substring(CHANNEL_PREFIX.length());
                ChatMessageResponse response = jsonMapper.readValue(body, ChatMessageResponse.class);

                // 현재 서버에 접속한 해당 채팅방 구독자들에게 브로드캐스팅
                messagingTemplate.convertAndSend("/sub/chat/" + roomId, response);
                log.debug("Redis Pub/Sub 수신 -> STOMP 발송 완료 - Room: {}, MessageId: {}", roomId, response.messageId());
            }
        } catch (Exception e) {
            log.error("Redis Pub/Sub 메시지 수신 후 처리 중 오류 발생", e);
        }
    }
}
