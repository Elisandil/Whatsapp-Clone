package com.whatsappclone.dtos.responses;

import com.whatsappclone.entities.message.MessageState;
import com.whatsappclone.entities.message.MessageType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageResponse {
    private Long id;
    private String content;
    private MessageType type;
    private MessageState state;
    private String senderId;
    private String receiverId;
    private LocalDateTime createdAt;
    private byte[] media;
}
