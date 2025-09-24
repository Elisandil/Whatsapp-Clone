package com.whatsappclone.services.user;

import com.whatsappclone.dtos.responses.UserResponse;
import com.whatsappclone.entities.user.User;
import com.whatsappclone.mappers.UserMapper;
import com.whatsappclone.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserResponse> getAllUsersExceptSelf(Authentication authUser) {
        String currentUserId = extractUserId(authUser);
        List<User> users = userRepository.findAllUsersExceptSelf(currentUserId);
        return mapToUserResponses(users);
    }

    public List<UserResponse> getAllUsersExceptSelfAndBlocked(Authentication authUser) {
        String currentUserId = extractUserId(authUser);
        List<User> users = userRepository.findAllUsersExceptSelfAndBlocked(currentUserId);
        return mapToUserResponses(users);
    }

    // -------------------------- PRIVATE METHODS -----------------------------
    private String extractUserId(Authentication authUser) {
        return authUser.getName();
    }

    private List<UserResponse> mapToUserResponses(List<User> users) {
        return users.stream()
                .map(userMapper::toUserResponse)
                .toList();
    }
}