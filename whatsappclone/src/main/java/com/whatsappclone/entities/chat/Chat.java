package com.whatsappclone.entities.chat;

import com.whatsappclone.entities.BaseAuditingEntity;
import com.whatsappclone.entities.message.Message;
import com.whatsappclone.entities.message.MessageState;
import com.whatsappclone.entities.message.MessageType;
import com.whatsappclone.entities.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chats")
@NamedQuery(name = ChatConstants.FIND_CHAT_BY_SENDER_ID,
        query = "SELECT DISTINCT c FROM Chat c WHERE c.sender.id = :senderId " +
                    "OR c.receiver.id = :senderId ORDER BY createdAt DESC")
@NamedQuery(name = ChatConstants.FIND_CHAT_BY_SENDER_ID_AND_RECEIVER_ID,
        query = "SELECT DISTINCT c FROM Chat c WHERE (c.sender.id = :senderId " +
                    "AND c.receiver.id = :receiverId) " +
                "OR (c.sender.id = :receiverId AND c.receiver.id = :senderId)")
public class Chat extends BaseAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String chatId;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @OneToMany(mappedBy = "chat", fetch = FetchType.EAGER)
    @OrderBy("createdAt DESC")
    private List<Message> messages;


    @Transient
    public String getChatName(final String SENDER_ID) {
        StringBuilder sb = new StringBuilder();

        if(receiver.getId().equals(SENDER_ID)) {
            return sb.append(sender.getFirstName()).append(" ").append(sender.getLastName()).toString();
        }
        return sb.append(receiver.getFirstName()).append(" ").append(receiver.getLastName()).toString();
    }

    @Transient
    public long getUnreadMessagesCount(final String SENDER_ID) {
        return messages.stream()
                .filter(m -> m.getReceiverId().equals(SENDER_ID))
                .filter(m -> MessageState.SENT == m.getState())
                .count();
    }

    @Transient
    public String getLastMessage(final String SENDER_ID) {

        if(messages != null && !messages.isEmpty()) {

            if(messages.getFirst().getType() != MessageType.TEXT) {
                return "Attachment";
            }
            return messages.getFirst().getContent();
        }
        return null;
    }

    @Transient
    public LocalDateTime getLastMessageTime() {

        if(messages != null && !messages.isEmpty()) {
            return messages.getFirst().getCreatedAt();
        }
        return null;
    }
}
