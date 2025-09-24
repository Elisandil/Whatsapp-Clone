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
        String creatorId = extractUserId(auth);
        User creator = findUserByPublicId(creatorId);
        List<User> members = buildMembersList(request.getMemberIds(), creator);

        Group group = buildGroup(request, creator, members);
        Group savedGroup = groupRepository.save(group);

        createGroupChat(savedGroup);

        return savedGroup.getGroupId();
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> getUserGroups(Authentication auth) {
        String userId = extractUserId(auth);
        return groupRepository.findGroupsByUserId(userId)
                .stream()
                .map(group -> groupMapper.toGroupResponse(group, userId))
                .toList();
    }

    @Transactional
    public void addMemberToGroup(String groupId, String userId, Authentication auth) {
        String requesterId = extractUserId(auth);
        Group group = findGroupById(groupId);

        validateAdminPermission(group, requesterId);

        User userToAdd = findUserByPublicId(userId);
        addUserToGroupIfNotMember(group, userToAdd, userId);
    }

    // -------------------------- PRIVATE METHODS -----------------------------
    private String extractUserId(Authentication auth) {
        return auth.getName();
    }

    private User findUserByPublicId(String userId) {
        return userRepository.findUserByPublicId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
    }

    private Group findGroupById(String groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with ID: " + groupId));
    }

    private List<User> buildMembersList(List<String> memberIds, User creator) {
        List<User> members = userRepository.findAllById(memberIds);

        if (!members.contains(creator)) {
            members.add(creator);
        }
        return members;
    }

    private Group buildGroup(GroupRequest request, User creator, List<User> members) {
        Group group = new Group();
        group.setGroupName(request.getGroupName());
        group.setDescription(request.getDescription());
        group.setCreatedBy(creator);
        group.setMembers(members);
        group.setAdmins(List.of(creator));

        return group;
    }

    private void createGroupChat(Group group) {
        Chat groupChat = new Chat();
        groupChat.setChatType(ChatType.GROUP);
        groupChat.setGroup(group);
        chatRepository.save(groupChat);
    }

    private void validateAdminPermission(Group group, String requesterId) {

        if (!group.isAdmin(requesterId)) {
            throw new SecurityException("Only admins can add members to group: " + group.getGroupId());
        }
    }

    private void addUserToGroupIfNotMember(Group group, User userToAdd, String userId) {

        if (!group.isMember(userId)) {
            group.getMembers().add(userToAdd);
            groupRepository.save(group);
        }
    }
}