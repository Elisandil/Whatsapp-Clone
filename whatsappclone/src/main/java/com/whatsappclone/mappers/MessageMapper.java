package com.whatsappclone.mappers;

import com.whatsappclone.dtos.responses.MessageResponse;
import com.whatsappclone.entities.message.Message;
import com.whatsappclone.utils.FileUtils;
import org.springframework.stereotype.Service;

@Service
public class MessageMapper {

    public MessageResponse toMessageResponse(Message msg) {
        return MessageResponse.builder()
                .id(msg.getId())
                .content(msg.getContent())
                .type(msg.getType())
                .state(msg.getState())
                .senderId(msg.getSenderId())
                .receiverId(msg.getReceiverId())
                .createdAt(msg.getCreatedDate())
                .media(FileUtils.readBytesFromFile(msg.getMediaPath()))
                .build();
    }
}
