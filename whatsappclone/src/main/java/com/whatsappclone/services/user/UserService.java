package com.whatsappclone.services.user;

import com.whatsappclone.dtos.responses.UserResponse;
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
        return userRepository.findAllUsersExceptSelf(authUser.getName())
                .stream()
                .map(userMapper::toUserResponse).toList();
    }

    // New
    public List<UserResponse> getAllUsersExceptSelfAndBlocked(Authentication authUser) {
        return userRepository.findAllUsersExceptSelfAndBlocked(authUser.getName())
                .stream()
                .map(userMapper::toUserResponse).toList();
    }

}
