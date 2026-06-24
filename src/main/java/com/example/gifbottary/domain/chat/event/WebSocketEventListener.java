package com.example.gifbottary.domain.chat.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("Received a new web socket connection. Session ID: {}", headerAccessor.getSessionId());
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("Web socket connection disconnected. Session ID: {}", headerAccessor.getSessionId());
        
        // TODO: 추후 JWT 구현 후 수정 예정 !! 유저 세션 맵핑을 통해 퇴장 시스템 메시지 발송이나 lastReadMessageId 업데이트 등을 처리할 수 있습니다.
    }
}
