package com.example.gifbottary.domain.chat.service;

import com.example.gifbottary.domain.chat.dto.request.ChatRoomCreateRequest;
import com.example.gifbottary.domain.chat.dto.response.ChatRoomCreateResponse;
import com.example.gifbottary.domain.chat.dto.response.ChatRoomListResponse;
import com.example.gifbottary.domain.chat.entity.ChatMember;
import com.example.gifbottary.domain.chat.entity.ChatRoom;
import com.example.gifbottary.domain.chat.repository.ChatMemberRepository;
import com.example.gifbottary.domain.chat.repository.ChatRoomRepository;
import com.example.gifbottary.domain.product.entity.GifticonSale;
import com.example.gifbottary.domain.product.repositroy.GifticonSaleRepository;
import com.example.gifbottary.domain.user.entity.User;
import com.example.gifbottary.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final GifticonSaleRepository gifticonSaleRepository;
    private final UserRepository userRepository;

    public List<ChatRoomListResponse> getRooms(Long userId) {
        return chatMemberRepository.findRoomListByUserId(userId);
    }

    @Transactional
    public ChatRoomCreateResponse createRoom(ChatRoomCreateRequest request) {
        // 1. 이미 존재하는 채팅방인지 검증 (존재하면 해당 방 ID 반환)
        Optional<ChatRoom> existingRoom = chatRoomRepository.findBySaleIdAndBuyerId(request.saleId(), request.buyerId());
        if (existingRoom.isPresent()) {
            return new ChatRoomCreateResponse(existingRoom.get().getId());
        }

        // 2. 데이터 조회
        GifticonSale sale = gifticonSaleRepository.findById(request.saleId())
                .orElseThrow();
        User buyer = userRepository.findById(request.buyerId())
                .orElseThrow();

        // 자신이 올린 판매글에 본인이 채팅방을 파는 것은 금지 (비즈니스 로직)
        if (sale.getSeller().getId().equals(buyer.getId())) {
            //TODO: 본인 판매글에 채팅방 생성 못하게 예외 발생, 추후 리팩토링
        }

        // 3. 채팅방 생성 및 저장
        ChatRoom chatRoom = new ChatRoom(sale, buyer);
        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        // 4. 참여자(구매자, 판매자)를 ChatMember에 등록
        ChatMember buyerMember = new ChatMember(savedRoom, buyer);
        ChatMember sellerMember = new ChatMember(savedRoom, sale.getSeller());
        chatMemberRepository.saveAll(List.of(buyerMember, sellerMember));

        return new ChatRoomCreateResponse(savedRoom.getId());
    }
}
