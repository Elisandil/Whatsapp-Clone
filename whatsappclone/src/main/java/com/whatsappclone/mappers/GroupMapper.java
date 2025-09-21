package com.whatsappclone.mappers;

import com.whatsappclone.dtos.responses.GroupResponse;
import com.whatsappclone.entities.group.Group;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupMapper {

    public GroupResponse toGroupResponse(Group group, String userId) {
        return GroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .description(group.getDescription())
                .groupPhoto(group.getGroupPhoto())
                .createdByName(group.getCreatedBy().getFirstName() + " " + group.getCreatedBy().getLastName())
                .memberCount(group.getMembers() != null ? group.getMembers().size() : 0)
                .memberNames(group.getMembers() != null ?
                        group.getMembers().stream()
                                .map(member -> member.getFirstName() + " " + member.getLastName())
                                .collect(Collectors.toList()) :
                        List.of())
                .createdAt(group.getCreatedDate())
                .isUserAdmin(group.isAdmin(userId))
                .build();
    }
}

