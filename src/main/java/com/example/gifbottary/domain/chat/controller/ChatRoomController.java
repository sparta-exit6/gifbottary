package com.example.gifbottary.domain.chat.controller;

import com.example.gifbottary.common.response.ApiResponse;
import com.example.gifbottary.domain.chat.dto.request.ChatRoomCreateRequest;
import com.example.gifbottary.domain.chat.dto.response.ChatRoomCreateResponse;
import com.example.gifbottary.domain.chat.dto.response.ChatMessageResponse;
import com.example.gifbottary.domain.chat.dto.response.ChatRoomListResponse;
import com.example.gifbottary.domain.chat.service.ChatMessageService;
import com.example.gifbottary.domain.chat.service.ChatRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @RequestParam("userId") Long userId // 추후 JWT AuthenticationPrincipal로 대체
    ) {
        List<ChatRoomListResponse> responses = chatRoomService.getRooms(userId);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getChatMessages(
            @PathVariable Long roomId,
            @RequestParam(value = "lastMessageId", required = false) Long lastMessageId,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        List<ChatMessageResponse> messages = chatMessageService.getMessages(roomId, lastMessageId, size);
        return ResponseEntity.ok(ApiResponse.ok(messages));
    }

    @GetMapping("/{roomId}/messages/missed")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMissedMessages(
            @PathVariable Long roomId,
            @RequestParam(value = "lastMessageId", required = false) Long lastMessageId
    ) {
        List<ChatMessageResponse> messages = chatMessageService.getMissedMessages(roomId, lastMessageId);
        return ResponseEntity.ok(ApiResponse.ok(messages));
    }

    @DeleteMapping("/{roomId}/members")
    public ResponseEntity<ApiResponse<Void>> leaveRoom(
            @PathVariable Long roomId,
            @RequestParam("userId") Long userId
    ) {
        chatRoomService.leaveRoom(roomId, userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChatRoomCreateResponse>> createRoom(
            @Valid @RequestBody ChatRoomCreateRequest request
    ) {
        ChatRoomCreateResponse response = chatRoomService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }
}
