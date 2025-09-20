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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final ChatMapper chatMapper;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ChatResponse> getChatsByReceiverId(Authentication currentUser) {
        final String userId = currentUser.getName();

        // Chats 1:1
        List<ChatResponse> directChats = chatRepository.findChatBySenderId(userId)
                .stream()
                .map(c -> chatMapper.toChatResponse(c, userId))
                .toList();

        // ChatGroup 1:N
        List<ChatResponse> groupChats = chatRepository.findGroupChatsByUserId(userId)
                .stream()
                .map(c -> chatMapper.toChatResponse(c, userId))
                .toList();

        List<ChatResponse> allChats = new ArrayList<>();
        allChats.addAll(directChats);
        allChats.addAll(groupChats);

        return allChats;
    }

    public String createChat(String senderId, String receiverId) {
        Optional<Chat> existingChat = chatRepository.findChatBySenderIdAndReceiverId(senderId, receiverId);

        if(existingChat.isPresent()) {
            return existingChat.get().getChatId();
        }
        User sender = userRepository.findUserByPublicId(senderId)
                .orElseThrow(() -> new EntityNotFoundException("Sender: " + senderId + " not found"));
        User receiver = userRepository.findUserByPublicId(receiverId)
                .orElseThrow(() -> new EntityNotFoundException("Receiver: " + receiverId + " not found"));
        Chat chat = new Chat();
        chat.setSender(sender);
        chat.setReceiver(receiver);

        Chat savedChat = chatRepository.save(chat);

        return savedChat.getChatId();
    }
}
