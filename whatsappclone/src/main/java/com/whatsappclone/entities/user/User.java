package com.whatsappclone.entities.user;

import com.whatsappclone.entities.chat.Chat;
import com.whatsappclone.entities.BaseAuditingEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
@NamedQuery(name = UserConstants.FIND_USER_BY_EMAIL,
        query = "SELECT u FROM User u WHERE u.email = :email")
@NamedQuery(name = UserConstants.FIND_ALL_USERS_EXCEPT_SELF,
        query = "SELECT u FROM User u WHERE u.id != :publicId") // publicId is the Keycloak ID
@NamedQuery(name = UserConstants.FIND_USER_BY_PUBLIC_ID,
        query = "SELECT u FROM User u WHERE u.id = :publicId")
//New
@NamedQuery(name = UserConstants.FIND_ALL_USERS_EXCEPT_SELF_AND_BLOCKED,
        query = """
            SELECT u FROM User u
            WHERE u.id != :publicId
            AND u.id NOT IN (
                SELECT bu.blockedId FROM BlockedUser bu WHERE bu.blockerId = :publicId
            )
            AND u.id NOT IN (
                SELECT bu.blockerId FROM BlockedUser bu WHERE bu.blockedId = :publicId
            )
            """)

public class User extends BaseAuditingEntity {
    private static final int LAST_ACTIVE_INTERVAL = 5;

    @Id
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private LocalDateTime lastSeen;

    @OneToMany(mappedBy = "sender")
    private List<Chat> chatsAsSender;

    @OneToMany(mappedBy = "receiver")
    private List<Chat> chatsAsReceiver;

    // ------- BlockedUser ----------------
    @OneToMany(mappedBy = "blockerId")
    private List<BlockedUser> usersBlocked;

    @OneToMany(mappedBy = "blockedId")
    private List<BlockedUser> blockedByUsers;
    // ------------------------------------

    @Transient
    public boolean isUserOnline() {
        return lastSeen != null &&
                lastSeen.isAfter(LocalDateTime.now().minusMinutes(LAST_ACTIVE_INTERVAL));
    }
}
