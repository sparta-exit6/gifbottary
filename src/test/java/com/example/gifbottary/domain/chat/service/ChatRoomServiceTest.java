package com.example.gifbottary.domain.chat.service;

import com.example.gifbottary.domain.chat.dto.request.ChatRoomCreateRequest;
import com.example.gifbottary.domain.chat.dto.response.ChatRoomCreateResponse;
import com.example.gifbottary.domain.chat.entity.ChatMember;
import com.example.gifbottary.domain.chat.entity.ChatRoom;
import com.example.gifbottary.domain.chat.repository.ChatMemberRepository;
import com.example.gifbottary.domain.chat.repository.ChatRoomRepository;
import com.example.gifbottary.domain.product.entity.GifticonSale;
import com.example.gifbottary.domain.product.repositroy.GifticonSaleRepository;
import com.example.gifbottary.domain.user.entity.User;
import com.example.gifbottary.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @InjectMocks
    private ChatRoomService chatRoomService;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMemberRepository chatMemberRepository;

    @Mock
    private GifticonSaleRepository gifticonSaleRepository;

    @Mock
    private UserRepository userRepository;

    @Captor
    private ArgumentCaptor<List<ChatMember>> chatMemberListCaptor;

    @Test
    @DisplayName("채팅방 생성 성공 - 정상적으로 방과 참여자가 생성되어야 한다")
    void createRoom_success() {
        // given
        Long saleId = 1L;
        Long buyerId = 2L;
        Long sellerId = 3L;

        ChatRoomCreateRequest request = new ChatRoomCreateRequest(saleId, buyerId);

        // 의존 관계 순서대로 Mock 설정 (Buyer, Seller -> Sale)
        User buyer = mock(User.class);
        when(buyer.getId()).thenReturn(buyerId);

        User seller = mock(User.class);
        when(seller.getId()).thenReturn(sellerId);

        GifticonSale sale = mock(GifticonSale.class);
        when(sale.getSeller()).thenReturn(seller);

        ChatRoom savedRoom = mock(ChatRoom.class);
        when(savedRoom.getId()).thenReturn(100L);

        when(chatRoomRepository.findBySaleIdAndBuyerId(saleId, buyerId)).thenReturn(Optional.empty());
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(gifticonSaleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(savedRoom);

        // when
        ChatRoomCreateResponse response = chatRoomService.createRoom(request);

        // then
        // [단위 테스트 한계] 이 단언문은 실제 ID 매핑 비즈니스 로직을 완벽히 검증하진 못하며, mock 객체가 반환한 100L이 응답 DTO에 제대로 담겨 내려가는지만 검증합니다.
        assertEquals(100L, response.roomId());

        verify(chatRoomRepository).save(any(ChatRoom.class));
        verify(chatMemberRepository).saveAll(chatMemberListCaptor.capture());

        // 참여자(구매자, 판매자)가 모두 정확하게 포함되어 저장되었는지 상세 검증 (AssertJ 활용)
        List<ChatMember> savedMembers = chatMemberListCaptor.getValue();
        assertThat(savedMembers)
                .hasSize(2)
                .extracting(m -> m.getUser().getId())
                .containsExactlyInAnyOrder(buyerId, sellerId);
    }
}
