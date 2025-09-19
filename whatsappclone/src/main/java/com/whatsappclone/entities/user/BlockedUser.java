package com.whatsappclone.entities.user;

import com.whatsappclone.entities.BaseAuditingEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "blocked_users",
        uniqueConstraints = @UniqueConstraint(columnNames = {"blocker_id", "blocked_id"}))
@NamedQuery(name = BlockedUserConstants.FIND_BLOCKED_USERS_BY_BLOCKER_ID,
        query = "SELECT bu FROM BlockedUser bu WHERE bu.blockerId = :blockerId")
@NamedQuery(name = BlockedUserConstants.IS_USER_BLOCKED,
        query = "SELECT COUNT(bu) > 0 FROM BlockedUser bu WHERE bu.blockerId = :blockerId AND bu.blockedId = :blockedId")
@NamedQuery(name = BlockedUserConstants.FIND_MUTUAL_BLOCK_STATUS,
        query = "SELECT bu FROM BlockedUser bu WHERE (bu.blockerId = :userId1 AND bu.blockedId = :userId2) OR (bu.blockerId = :userId2 AND bu.blockedId = :userId1)")
public class BlockedUser extends BaseAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "blocker_id", nullable = false)
    private String blockerId;

    @Column(name = "blocked_id", nullable = false)
    private String blockedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id", insertable = false, updatable = false)
    private User blocker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id", insertable = false, updatable = false)
    private User blocked;
    private String reason;
}

