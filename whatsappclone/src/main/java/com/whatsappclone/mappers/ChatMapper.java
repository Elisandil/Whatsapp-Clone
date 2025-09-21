package com.whatsappclone.mappers;

import com.whatsappclone.dtos.responses.ChatResponse;
import com.whatsappclone.entities.chat.Chat;
import org.springframework.stereotype.Service;

@Service
public class ChatMapper {

    public ChatResponse toChatResponse(Chat chat, String senderId) {
        return ChatResponse.builder()
                .id(chat.getChatId())
                .name(chat.getChatName(senderId))
                .unreadCount(chat.getUnreadMessagesCount(senderId))
                .lastMessage(chat.getLastMessage(senderId))
                .lastMessageCreatedAt(chat.getLastMessageTime())
                .isReceiverOnline(chat.getReceiver().isUserOnline())
                .senderId(chat.getSender().getId())
                .receiverId(chat.getReceiver().getId())
                .build();
    }
}
