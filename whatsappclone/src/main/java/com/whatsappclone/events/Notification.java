package com.whatsappclone.events;

import com.whatsappclone.entities.message.MessageType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    private String chatId;
    private String content;
    private String senderId;
    private String receiverId;
    private String chatName;
    private MessageType msgType;
    private NotificationType type;
    private byte[] media;
}
