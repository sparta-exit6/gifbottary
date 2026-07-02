package com.example.gifbottary.domain.chat.service;

import com.example.gifbottary.common.exception.ErrorCode;
import com.example.gifbottary.common.exception.ServiceException;
import com.example.gifbottary.domain.chat.dto.request.ChatRoomCreateRequest;
import com.example.gifbottary.domain.chat.dto.response.ChatMessageResponse;
import com.example.gifbottary.domain.chat.dto.response.ChatRoomCreateResponse;
import com.example.gifbottary.domain.chat.dto.response.ChatRoomListResponse;
import com.example.gifbottary.domain.chat.entity.ChatMember;
import com.example.gifbottary.domain.chat.entity.ChatRoom;
import com.example.gifbottary.domain.chat.repository.ChatMemberRepository;
import com.example.gifbottary.domain.chat.repository.ChatRoomRepository;
import com.example.gifbottary.domain.product.entity.GifticonSale;
import com.example.gifbottary.domain.product.repository.GifticonSaleRepository;
import com.example.gifbottary.domain.user.entity.User;
import com.example.gifbottary.domain.user.repository.UserRepository;
import tools.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    public static final String ENTER_MESSAGE_FORMAT = "%s님이 입장했습니다.";
    public static final String LEAVE_MESSAGE_FORMAT = "%s님이 채팅방을 나갔습니다.";

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final GifticonSaleRepository gifticonSaleRepository;
    private final UserRepository userRepository;
    private final ChatMessageService chatMessageService;
    private final StringRedisTemplate stringRedisTemplate;
    private final JsonMapper jsonMapper;

    public List<ChatRoomListResponse> getRooms(Long userId) {
        return chatRoomRepository.findRoomListByUserId(userId);
    }

    @Transactional
    public ChatRoomCreateResponse createRoom(ChatRoomCreateRequest request, Long buyerId) {
        // 1. 이미 존재하는 채팅방인지 검증 (존재하면 해당 방 ID 반환)
        Optional<ChatRoom> existingRoom = chatRoomRepository.findBySaleIdAndBuyerId(request.saleId(), buyerId);
        if (existingRoom.isPresent()) {
            return new ChatRoomCreateResponse(existingRoom.get().getId());
        }

        // 2. 데이터 조회
        GifticonSale sale = gifticonSaleRepository.findById(request.saleId())
                .orElseThrow(() -> new ServiceException(ErrorCode.PRODUCT_NOT_FOUND));
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        // 자신이 올린 판매글에 본인이 채팅방을 파는 것은 금지 (비즈니스 로직)
        if (sale.getSeller().getId().equals(buyer.getId())) {
            throw new ServiceException(ErrorCode.CANNOT_CHAT_WITH_SELF);
        }

        // 3. 채팅방 생성 및 저장
        ChatRoom chatRoom = new ChatRoom(sale, buyer);
        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        // 4. 참여자(구매자, 판매자)를 ChatMember에 등록
        ChatMember buyerMember = new ChatMember(savedRoom, buyer);
        ChatMember sellerMember = new ChatMember(savedRoom, sale.getSeller());
        chatMemberRepository.saveAll(List.of(buyerMember, sellerMember));

        // 5. 최초 개설 시스템 메시지 DB 각인 및 Redis Pub/Sub 발행
        String enterMsg = String.format(ENTER_MESSAGE_FORMAT, buyer.getName());
        ChatMessageResponse enterResponse = chatMessageService.saveSystemMessage(savedRoom.getId(), buyer.getId(), enterMsg);
        publishSystemMessage(savedRoom.getId(), enterResponse);

        return new ChatRoomCreateResponse(savedRoom.getId());
    }

    @Transactional
    public void leaveRoom(Long roomId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        ChatMember member = chatMemberRepository.findByChatRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.ALREADY_EXITED_CHATROOM));

        chatMemberRepository.delete(member);

        String leaveMsg = String.format(LEAVE_MESSAGE_FORMAT, user.getName());
        ChatMessageResponse response = chatMessageService.saveSystemMessage(roomId, userId, leaveMsg);
        publishSystemMessage(roomId, response);
    }

    private void publishSystemMessage(Long roomId, ChatMessageResponse response) {
        try {
            String json = jsonMapper.writeValueAsString(response);
            stringRedisTemplate.convertAndSend("chat-room:" + roomId, json);
        } catch (Exception e) {
            log.error("Redis Pub/Sub 시스템 메시지 발행 실패 - Room: {}", roomId, e);
        }
    }
}
