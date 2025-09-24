package com.whatsappclone.services;

import com.whatsappclone.dtos.responses.ChatResponse;
import com.whatsappclone.entities.chat.Chat;
import com.whatsappclone.entities.user.User;
import com.whatsappclone.mappers.ChatMapper;
import com.whatsappclone.repositories.ChatRepository;
import com.whatsappclone.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final ChatMapper chatMapper;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ChatResponse> getChatsByReceiverId(Authentication currentUser) {
        final String userId = currentUser.getName();
        List<ChatResponse> directChats = getDirectChats(userId);
        List<ChatResponse> groupChats = getGroupChats(userId);

        return Stream.concat(directChats.stream(), groupChats.stream())
                .collect(Collectors.toList());
    }

    public String createChat(String senderId, String receiverId) {
        return chatRepository.findChatBySenderIdAndReceiverId(senderId, receiverId)
                .map(Chat::getChatId)
                .orElseGet(() -> createNewChat(senderId, receiverId));
    }

    private List<ChatResponse> getDirectChats(String userId) {
        return chatRepository.findChatBySenderId(userId)
                .stream()
                .map(chat -> chatMapper.toChatResponse(chat, userId))
                .collect(Collectors.toList());
    }

    private List<ChatResponse> getGroupChats(String userId) {
        return chatRepository.findGroupChatsByUserId(userId)
                .stream()
                .map(chat -> chatMapper.toChatResponse(chat, userId))
                .collect(Collectors.toList());
    }

    private String createNewChat(String senderId, String receiverId) {
        User sender = findUserByPublicId(senderId, "Sender");
        User receiver = findUserByPublicId(receiverId, "Receiver");

        Chat chat = buildChat(sender, receiver);
        Chat savedChat = chatRepository.save(chat);

        return savedChat.getChatId();
    }

    private User findUserByPublicId(String publicId, String userType) {
        return userRepository.findUserByPublicId(publicId)
                .orElseThrow(() -> new EntityNotFoundException(userType + ": " + publicId + " not found"));
    }

    private Chat buildChat(User sender, User receiver) {
        Chat chat = new Chat();
        chat.setSender(sender);
        chat.setReceiver(receiver);
        return chat;
    }
}
