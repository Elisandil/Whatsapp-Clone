package com.whatsappclone.dtos.requests;

import com.whatsappclone.entities.message.MessageType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageRequest {
    private String content;
    private String senderId;
    private String receiverId;
    private MessageType type;
    private String chatId;
}
