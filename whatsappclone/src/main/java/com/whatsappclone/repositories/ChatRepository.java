package com.whatsappclone.repositories;

import com.whatsappclone.entities.chat.Chat;
import com.whatsappclone.entities.chat.ChatConstants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, String> {

    @Query(name = ChatConstants.FIND_CHAT_BY_SENDER_ID)
    List<Chat> findChatBySenderId(@Param("senderId") String senderId);

    @Query(name = ChatConstants.FIND_CHAT_BY_SENDER_ID_AND_RECEIVER_ID)
    Optional<Chat> findChatBySenderIdAndReceiverId(@Param("senderId") String senderId,
                                                   @Param("receiverId") String receiverId);

    // New
    @Query(name = ChatConstants.FIND_GROUP_CHATS_BY_USER_ID)
    List<Chat> findGroupChatsByUserId(@Param("userId") String userId);

}
