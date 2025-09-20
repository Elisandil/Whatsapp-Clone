package com.whatsappclone.services;

import com.whatsappclone.dtos.requests.MessageRequest;
import com.whatsappclone.dtos.responses.MessageResponse;
import com.whatsappclone.entities.chat.Chat;
import com.whatsappclone.entities.message.Message;
import com.whatsappclone.entities.message.MessageState;
import com.whatsappclone.entities.message.MessageType;
import com.whatsappclone.events.Notification;
import com.whatsappclone.events.NotificationType;
import com.whatsappclone.mappers.MessageMapper;
import com.whatsappclone.repositories.ChatRepository;
import com.whatsappclone.repositories.MessageRepository;
import com.whatsappclone.services.user.BlockedUserService;
import com.whatsappclone.utils.FileUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final MessageMapper messageMapper;
    private final FileService fileService;
    private final NotificationService notificationService;
    private final BlockedUserService blockedUserService;


    public void saveMessage(MessageRequest request) {

        if (blockedUserService.areUsersBlocked(request.getSenderId(), request.getReceiverId())) {
            throw new IllegalStateException("Cannot send message to blocked user");
        }
        Chat chat = chatRepository.findById(request.getChatId())
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

        Message msg = new Message();
        msg.setContent(request.getContent());
        msg.setChat(chat);
        msg.setSenderId(request.getSenderId());
        msg.setReceiverId(request.getReceiverId());
        msg.setType(request.getType());
        msg.setState(MessageState.SENT);

        messageRepository.save(msg);
        updateMessageState(msg.getId(), MessageState.DELIVERED);

        Notification notification = Notification.builder()
                .chatId(chat.getChatId())
                .msgType(request.getType())
                .content(request.getContent())
                .senderId(request.getSenderId())
                .receiverId(request.getReceiverId())
                .type(NotificationType.MESSAGE)
                .chatName(chat.getChatName(request.getSenderId()))
                .build();

        notificationService.sendNotification(msg.getReceiverId(), notification);
    }

    public List<MessageResponse> findChatMessages(String chatId) {
        return messageRepository.findMessagesByChatId(chatId)
                .stream()
                .map(messageMapper::toMessageResponse).toList();
    }

    @Transactional
    public void setMessageToSeen(String chatId, Authentication auth) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new EntityNotFoundException("Chat not found"));
        final String receiverId = getReceiverId(chat, auth);
        messageRepository.setMessagesToSeenByChat(chatId, MessageState.SEEN);

        Notification notification = Notification.builder()
                .chatId(chat.getChatId())
                .senderId(getSenderId(chat, auth))
                .receiverId(receiverId)
                .type(NotificationType.SEEN)
                .build();

        notificationService.sendNotification(receiverId, notification);
    }

    public void uploadMediaMessage(String chatId, MultipartFile file, Authentication auth) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new EntityNotFoundException("Chat not found"));
        final String senderId = getSenderId(chat, auth);
        final String receiverId = getReceiverId(chat, auth);
        final String filePath = fileService.saveFile(file, senderId);
        Message msg = new Message();

        msg.setChat(chat);
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setType(MessageType.IMAGE);
        msg.setState(MessageState.SENT);
        msg.setMediaPath(filePath);

        messageRepository.save(msg);

        Notification notification = Notification.builder()
                .chatId(chat.getChatId())
                .msgType(MessageType.IMAGE)
                .senderId(senderId)
                .receiverId(receiverId)
                .type(NotificationType.IMAGE)
                .media(FileUtils.readBytesFromFile(filePath))
                .build();

        notificationService.sendNotification(receiverId, notification);
    }

    // New
    public void updateMessageState(Long messageId, MessageState newState) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        if (isValidStateTransition(message.getState(), newState)) {
            message.setState(newState);
            messageRepository.save(message);

            Notification notification = Notification.builder()
                    .chatId(message.getChat().getChatId())
                    .msgType(message.getType())
                    .senderId(message.getReceiverId())
                    .receiverId(message.getSenderId())
                    .type(getNotificationTypeForState(newState))
                    .build();

            notificationService.sendNotification(message.getSenderId(), notification);
        }
    }



    // ---------------------- PRIVATE METHODS ----------------------------------
    private String getSenderId(Chat chat, Authentication auth) {

        if(chat.getSender().getId().equals(auth.getName())) {
            return chat.getSender().getId();
        }
        return chat.getReceiver().getId();
    }

    private String getReceiverId(Chat chat, Authentication auth) {

        if(chat.getSender().getId().equals(auth.getName())) {
            return chat.getReceiver().getId();
        }
        return chat.getSender().getId();
    }

    // New
    private boolean isValidStateTransition(MessageState currentState, MessageState newState) {
        return switch (currentState) {
            case SENDING -> newState == MessageState.SENT || newState == MessageState.FAILED;
            case SENT -> newState == MessageState.DELIVERED || newState == MessageState.FAILED;
            case DELIVERED -> newState == MessageState.SEEN;
            case SEEN, FAILED -> false;
        };
    }

    private NotificationType getNotificationTypeForState(MessageState state) {
        return switch (state) {
            case DELIVERED -> NotificationType.DELIVERED;
            case SEEN -> NotificationType.SEEN;
            default -> NotificationType.MESSAGE;
        };
    }
}
