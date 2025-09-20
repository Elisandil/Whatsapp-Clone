package com.whatsappclone.services.user;

import com.whatsappclone.dtos.requests.BlockedUserRequest;
import com.whatsappclone.dtos.responses.BlockedUserResponse;
import com.whatsappclone.entities.user.BlockedUser;
import com.whatsappclone.mappers.BlockedUserMapper;
import com.whatsappclone.repositories.BlockedUserRepository;
import com.whatsappclone.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockedUserService {
    private final BlockedUserRepository blockedUserRepository;
    private final UserRepository userRepository;
    private final BlockedUserMapper blockedUserMapper;

    @Transactional
    public void blockUser(Authentication auth, BlockedUserRequest request) {
        String blockerId = auth.getName();
        String blockedId = request.getUserIdToBlock();

        if (blockerId.equals(blockedId)) {
            throw new IllegalArgumentException("User cannot block himself");
        }

        if (blockedUserRepository.isUserBlocked(blockerId, blockedId)) {
            throw new IllegalArgumentException("User is already blocked");
        }
        BlockedUser blockedUser = new BlockedUser();
        blockedUser.setBlockerId(blockerId);
        blockedUser.setBlockedId(blockedId);
        blockedUser.setReason(request.getReason());

        blockedUserRepository.save(blockedUser);
    }

    @Transactional
    public void unblockUser(Authentication auth, String userIdToUnblock) {
        String blockerId = auth.getName();

        BlockedUser blockedUser = blockedUserRepository.findByBlockerIdAndBlockedId(blockerId, userIdToUnblock)
                .orElseThrow(() -> new EntityNotFoundException("Blocked user not found"));

        blockedUserRepository.delete(blockedUser);
    }

    @Transactional(readOnly = true)
    public List<BlockedUserResponse> getBlockedUsers(Authentication auth) {
        String userId = auth.getName();

        return blockedUserRepository.findBlockedUsersByBlockerId(userId)
                .stream()
                .map(blockedUserMapper::toBlockedUserResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isUserBlocked(String blockerId, String blockedId) {
        return blockedUserRepository.isUserBlocked(blockerId, blockedId);
    }

    @Transactional(readOnly = true)
    public boolean areUsersBlocked(String userId1, String userId2) {
        List<BlockedUser> blocks = blockedUserRepository.findMutualBlockStatus(userId1, userId2);
        return !blocks.isEmpty();
    }
}
