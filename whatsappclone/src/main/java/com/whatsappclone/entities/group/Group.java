package com.whatsappclone.entities.group;

import com.whatsappclone.entities.BaseAuditingEntity;
import com.whatsappclone.entities.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "groups")
@NamedQuery(name = GroupConstants.FIND_GROUPS_BY_USER_ID,
        query = "SELECT g FROM Group g JOIN g.members m WHERE m.id = :userId")
public class Group extends BaseAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String groupId;
    private String groupName;
    private String description;
    private String groupPhoto;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> members;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "group_admins",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> admins;


    @Transient
    public boolean isAdmin(final String userId) {
        return admins.stream().anyMatch(admin -> admin.getId().equals(userId));
    }

    @Transient
    public boolean isMember(final String userId) {
        return members.stream().anyMatch(member -> member.getId().equals(userId));
    }
}
