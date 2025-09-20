package com.whatsappclone.dtos.responses;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatResponse {
    private String id;
    private String name;
    private long unreadCount;
    private String lastMessage;
    private LocalDateTime lastMessageCreatedAt;
    private boolean isReceiverOnline;
    private String senderId;
    private String receiverId;
}
