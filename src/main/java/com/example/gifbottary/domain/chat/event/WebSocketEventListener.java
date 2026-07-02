package com.example.gifbottary.domain.chat.event;

import com.example.gifbottary.domain.chat.dto.StompPrincipal;
import com.example.gifbottary.domain.chat.service.ChatMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private record SessionRoomInfo(Long roomId, StompPrincipal principal) {}
    private final Map<String, SessionRoomInfo> sessionRoomMap = new ConcurrentHashMap<>();
    private final ChatMemberService chatMemberService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("Received a new web socket connection. Session ID: {}", headerAccessor.getSessionId());
    }

    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        Principal principal = accessor.getUser();

        if (destination != null && destination.startsWith("/sub/chat/") && principal instanceof StompPrincipal stompPrincipal) {
            try {
                Long roomId = Long.parseLong(destination.substring("/sub/chat/".length()));
                String sessionId = accessor.getSessionId();

                sessionRoomMap.put(sessionId, new SessionRoomInfo(roomId, stompPrincipal));
                log.info("채팅방 소켓 구독 완료 (알림성 입장 메시지 미발송) - Room: {}, User: {}", roomId, stompPrincipal.userName());
            } catch (NumberFormatException e) {
                log.warn("구독 경로에서 roomId 파싱 실패: {}", destination);
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        SessionRoomInfo info = sessionRoomMap.remove(sessionId);
        if (info != null) {
            chatMemberService.updateLastReadMessageId(info.roomId(), info.principal().userId());
            log.info("일시적 소켓 단절 감지 (마지막 수신 커서 DB 동기화 완료) - Room: {}, User: {}", info.roomId(), info.principal().userName());
        } else {
            log.info("Web socket connection disconnected without room subscription. Session ID: {}", sessionId);
        }
    }
}
