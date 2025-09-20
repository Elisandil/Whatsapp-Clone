package com.whatsappclone.services;

import com.whatsappclone.dtos.requests.GroupRequest;
import com.whatsappclone.dtos.responses.GroupResponse;
import com.whatsappclone.entities.chat.Chat;
import com.whatsappclone.entities.chat.ChatType;
import com.whatsappclone.entities.group.Group;
import com.whatsappclone.entities.user.User;
import com.whatsappclone.mappers.GroupMapper;
import com.whatsappclone.repositories.ChatRepository;
import com.whatsappclone.repositories.GroupRepository;
import com.whatsappclone.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final GroupMapper groupMapper;

    @Transactional
    public String createGroup(GroupRequest request, Authentication auth) {
        String creatorId = auth.getName();

        User creator = userRepository.findUserByPublicId(creatorId)
                .orElseThrow(() -> new EntityNotFoundException("Creator not found"));

        List<User> members = userRepository.findAllById(request.getMemberIds());

        if (!members.contains(creator)) {
            members.add(creator);
        }
        Group group = new Group();
        group.setGroupName(request.getGroupName());
        group.setDescription(request.getDescription());
        group.setCreatedBy(creator);
        group.setMembers(members);
        group.setAdmins(List.of(creator));

        Group savedGroup = groupRepository.save(group);

        Chat groupChat = new Chat();
        groupChat.setChatType(ChatType.GROUP);
        groupChat.setGroup(savedGroup);
        chatRepository.save(groupChat);

        return savedGroup.getGroupId();
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> getUserGroups(Authentication auth) {
        String userId = auth.getName();
        return groupRepository.findGroupsByUserId(userId)
                .stream()
                .map(group -> groupMapper.toGroupResponse(group, userId))
                .toList();
    }


    @Transactional
    public void addMemberToGroup(String groupId, String userId, Authentication auth) {
        String requesterId = auth.getName();

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));

        if (!group.isAdmin(requesterId)) {
            throw new SecurityException("Only admins can add members");
        }

        User userToAdd = userRepository.findUserByPublicId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!group.isMember(userId)) {
            group.getMembers().add(userToAdd);
            groupRepository.save(group);
        }
    }


}
