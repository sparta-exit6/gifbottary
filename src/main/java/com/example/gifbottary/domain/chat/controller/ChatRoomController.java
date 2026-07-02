package com.example.gifbottary.domain.chat.controller;

import com.example.gifbottary.common.response.ApiResponse;
import com.example.gifbottary.domain.chat.dto.request.ChatMessageListRequest;
import com.example.gifbottary.domain.chat.dto.request.ChatRoomCreateRequest;
import com.example.gifbottary.domain.chat.dto.request.ChatMissedMessageRequest;
import com.example.gifbottary.domain.chat.dto.response.ChatRoomCreateResponse;
import com.example.gifbottary.domain.chat.dto.response.ChatMessageResponse;
import com.example.gifbottary.domain.chat.dto.response.ChatRoomListResponse;
import com.example.gifbottary.domain.chat.service.ChatMessageService;
import com.example.gifbottary.domain.chat.service.ChatRoomService;
import com.example.gifbottary.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chatrooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatRoomListResponse>>> getRooms(
            @AuthenticationPrincipal User user
    ) {
        List<ChatRoomListResponse> responses = chatRoomService.getRooms(user.getId());
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getChatMessages(
            @PathVariable Long roomId,
            @Valid @ModelAttribute ChatMessageListRequest request,
            @AuthenticationPrincipal User user
    ) {
        List<ChatMessageResponse> messages = chatMessageService.getMessages(roomId, request, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(messages));
    }

    @GetMapping("/{roomId}/messages/missed")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMissedMessages(
            @PathVariable Long roomId,
            @Valid @ModelAttribute ChatMissedMessageRequest request,
            @AuthenticationPrincipal User user
    ) {
        List<ChatMessageResponse> messages = chatMessageService.getMissedMessages(roomId, request, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(messages));
    }

    @DeleteMapping("/{roomId}/members")
    public ResponseEntity<ApiResponse<Void>> leaveRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal User user
            ) {
        chatRoomService.leaveRoom(roomId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChatRoomCreateResponse>> createRoom(
            @Valid @RequestBody ChatRoomCreateRequest request,
            @AuthenticationPrincipal User user
    ) {
        ChatRoomCreateResponse response = chatRoomService.createRoom(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }
}
