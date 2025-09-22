package com.whatsappclone.controllers;

import com.whatsappclone.dtos.responses.ChatResponse;
import com.whatsappclone.dtos.responses.StringResponse;
import com.whatsappclone.services.ChatService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
@Tag(name = "Chat")
public class ChatController {
    private final ChatService chatService;


    // ------------------- POST METHODS ---------------------------------------
    @PostMapping
    public ResponseEntity<StringResponse> createChat(
            @RequestParam(name = "sender-id") String senderId,
            @RequestParam(name = "receiver-id") String receiverId) {

        final String chatId = chatService.createChat(senderId, receiverId);
        StringResponse strResponse = StringResponse.builder().message(chatId).build();

        return ResponseEntity.ok(strResponse);
    }

    // ------------------- GET METHODS ---------------------------------------
    @GetMapping
    public ResponseEntity<List<ChatResponse>> getChatsByReceiverId(Authentication auth) {
        return ResponseEntity.ok(chatService.getChatsByReceiverId(auth));
    }
}
