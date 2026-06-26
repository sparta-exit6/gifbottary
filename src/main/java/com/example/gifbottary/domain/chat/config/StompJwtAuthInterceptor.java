package com.example.gifbottary.domain.chat.config;

import com.example.gifbottary.common.exception.ErrorCode;
import com.example.gifbottary.common.exception.ServiceException;
import com.example.gifbottary.domain.auth.jwt.JwtProvider;
import com.example.gifbottary.domain.chat.dto.StompPrincipal;
import com.example.gifbottary.domain.user.entity.User;
import com.example.gifbottary.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompJwtAuthInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;
    // 추후 리팩토링 시 JWT Token에 userName 넣으면 없앨 수 있어 좋을듯합니다.
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("STOMP CONNECT - Authorization 헤더가 누락되었거나 Bearer 형식이 아닙니다.");
                throw new ServiceException(ErrorCode.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            if (!jwtProvider.validateToken(token)) {
                log.warn("STOMP CONNECT - 유효하지 않은 토큰입니다.");
                throw new ServiceException(ErrorCode.INVALID_TOKEN);
            }

            Long userId = jwtProvider.getUserId(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

            StompPrincipal principal = new StompPrincipal(userId, user.getName());
            accessor.setUser(principal);
            log.info("STOMP CONNECT 인증 성공 - User ID: {}, Name: {}", userId, user.getName());
        }

        return message;
    }
}
