package com.example.gifbottary.domain.chat.controller;

import com.example.gifbottary.domain.chat.dto.response.ChatMessageResponse;
import com.example.gifbottary.domain.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatMessageService chatMessageService;

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getChatMessages(
            @PathVariable Long roomId,
            @RequestParam(value = "lastMessageId", required = false) Long lastMessageId,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        List<ChatMessageResponse> messages = chatMessageService.getMessages(roomId, lastMessageId, size);
        return ResponseEntity.ok(messages);
    }
}
