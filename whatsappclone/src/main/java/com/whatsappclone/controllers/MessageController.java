package com.whatsappclone.controllers;

import com.whatsappclone.dtos.requests.MessageRequest;
import com.whatsappclone.dtos.responses.MessageResponse;
import com.whatsappclone.services.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Tag(name = "Message")
public class MessageController {
    private final MessageService messageService;


    // ------------------- POST METHODS ---------------------------------------
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Save message", description = "Save a message")
    public void saveMessage(@RequestBody MessageRequest request) {
        messageService.saveMessage(request);
    }

    @PostMapping(value = "/upload-media", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload media", description = "Upload files in a message")
    public void uploadMedia(@RequestParam("chat-id") String chatId,
                            @Parameter()
                            @RequestParam("file") MultipartFile file,
                            Authentication auth) {

        messageService.uploadMediaMessage(chatId, file, auth);
    }

    // ------------------- PATCH METHODS ---------------------------------------
    @PatchMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Set message to seen", description = "Set a target message to a seen state")
    public void setMessageToSeen(@RequestParam("chat-id") String chatId,
                                 Authentication auth) {

        messageService.setMessageToSeen(chatId, auth);
    }

    // ------------------- GET METHODS ---------------------------------------
    @GetMapping("/chat/{chat-id}")
    @Operation(summary = "Get messages", description = "Get all the messages from a target chat")
    public ResponseEntity<List<MessageResponse>> getMessages(@PathVariable("chat-id") String chatId) {
        return ResponseEntity.ok(messageService.findChatMessages(chatId));
    }
}