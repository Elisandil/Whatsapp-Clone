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
        validateMessageRequest(request);

        Chat chat = findChatById(request.getChatId());
        Message message = createMessage(request, chat);

        messageRepository.save(message);
        updateMessageState(message.getId(), MessageState.DELIVERED);

        sendMessageNotification(request, chat);
    }

    public List<MessageResponse> findChatMessages(String chatId) {
        return messageRepository.findMessagesByChatId(chatId)
                .stream()
                .map(messageMapper::toMessageResponse)
                .toList();
    }

    @Transactional
    public void setMessageToSeen(String chatId, Authentication auth) {
        Chat chat = findChatById(chatId);
        String receiverId = getReceiverId(chat, auth);
        String senderId = getSenderId(chat, auth);

        messageRepository.setMessagesToSeenByChat(chatId, MessageState.SEEN);
        sendSeenNotification(chat, senderId, receiverId);
    }

    public void uploadMediaMessage(String chatId, MultipartFile file, Authentication auth) {
        Chat chat = findChatById(chatId);
        String senderId = getSenderId(chat, auth);
        String receiverId = getReceiverId(chat, auth);

        String filePath = fileService.saveFile(file, senderId);
        Message message = createMediaMessage(chat, senderId, receiverId, filePath);

        messageRepository.save(message);
        sendMediaNotification(chat, senderId, receiverId, filePath);
    }

    public void updateMessageState(Long messageId, MessageState newState) {
        Message message = findMessageById(messageId);

        if (isValidStateTransition(message.getState(), newState)) {
            updateMessageStateAndNotify(message, newState);
        }
    }

    // ---------------------- PRIVATE METHODS ----------------------------------
    private void validateMessageRequest(MessageRequest request) {

        if (blockedUserService.areUsersBlocked(request.getSenderId(), request.getReceiverId())) {
            throw new IllegalStateException("Cannot send message to blocked user");
        }
    }

    private Chat findChatById(String chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));
    }

    private Message findMessageById(Long messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));
    }

    private Message createMessage(MessageRequest request, Chat chat) {
        Message message = new Message();
        message.setContent(request.getContent());
        message.setChat(chat);
        message.setSenderId(request.getSenderId());
        message.setReceiverId(request.getReceiverId());
        message.setType(request.getType());
        message.setState(MessageState.SENT);
        return message;
    }

    private Message createMediaMessage(Chat chat, String senderId, String receiverId, String filePath) {
        Message message = new Message();
        message.setChat(chat);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setType(MessageType.IMAGE);
        message.setState(MessageState.SENT);
        message.setMediaPath(filePath);
        return message;
    }

    private void sendMessageNotification(MessageRequest request, Chat chat) {
        Notification notification = createNotificationBuilder(chat, request.getSenderId(), request.getReceiverId())
                .msgType(request.getType())
                .content(request.getContent())
                .type(NotificationType.MESSAGE)
                .chatName(chat.getTargetChatName(request.getSenderId()))
                .build();

        notificationService.sendNotification(request.getReceiverId(), notification);
    }

    private void sendSeenNotification(Chat chat, String senderId, String receiverId) {
        Notification notification = createNotificationBuilder(chat, senderId, receiverId)
                .type(NotificationType.SEEN)
                .build();

        notificationService.sendNotification(receiverId, notification);
    }

    private void sendMediaNotification(Chat chat, String senderId, String receiverId, String filePath) {
        Notification notification = createNotificationBuilder(chat, senderId, receiverId)
                .msgType(MessageType.IMAGE)
                .type(NotificationType.IMAGE)
                .media(FileUtils.readBytesFromFile(filePath))
                .build();

        notificationService.sendNotification(receiverId, notification);
    }

    private void updateMessageStateAndNotify(Message message, MessageState newState) {
        message.setState(newState);
        messageRepository.save(message);

        Notification notification = createNotificationBuilder(
                message.getChat(),
                message.getReceiverId(),
                message.getSenderId())
                .msgType(message.getType())
                .type(getNotificationTypeForState(newState))
                .build();

        notificationService.sendNotification(message.getSenderId(), notification);
    }

    private Notification.NotificationBuilder createNotificationBuilder(Chat chat, String senderId, String receiverId) {
        return Notification.builder()
                .chatId(chat.getChatId())
                .senderId(senderId)
                .receiverId(receiverId);
    }

    private String getSenderId(Chat chat, Authentication auth) {
        String authId = auth.getName();
        return chat.getSender().getId().equals(authId)
                ? chat.getSender().getId()
                : chat.getReceiver().getId();
    }

    private String getReceiverId(Chat chat, Authentication auth) {
        String authId = auth.getName();
        return chat.getSender().getId().equals(authId)
                ? chat.getReceiver().getId()
                : chat.getSender().getId();
    }

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
