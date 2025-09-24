package com.whatsappclone.services.user;

import com.whatsappclone.dtos.requests.BlockedUserRequest;
import com.whatsappclone.dtos.responses.BlockedUserResponse;
import com.whatsappclone.entities.user.BlockedUser;
import com.whatsappclone.mappers.BlockedUserMapper;
import com.whatsappclone.repositories.BlockedUserRepository;
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
    private final BlockedUserMapper blockedUserMapper;

    @Transactional
    public void blockUser(Authentication auth, BlockedUserRequest request) {
        String blockerId = extractUserId(auth);
        String blockedId = request.getUserIdToBlock();

        validateBlockRequest(blockerId, blockedId);

        BlockedUser blockedUser = createBlockedUser(blockerId, blockedId, request.getReason());
        blockedUserRepository.save(blockedUser);
    }

    @Transactional
    public void unblockUser(Authentication auth, String userIdToUnblock) {
        String blockerId = extractUserId(auth);

        BlockedUser blockedUser = findExistingBlock(blockerId, userIdToUnblock);
        blockedUserRepository.delete(blockedUser);
    }

    @Transactional(readOnly = true)
    public List<BlockedUserResponse> getBlockedUsers(Authentication auth) {
        String userId = extractUserId(auth);

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

    // -------------------------- PRIVATE METHODS -----------------------------
    private String extractUserId(Authentication auth) {
        return auth.getName();
    }

    private void validateBlockRequest(String blockerId, String blockedId) {
        validateSelfBlock(blockerId, blockedId);
        validateDuplicateBlock(blockerId, blockedId);
    }

    private void validateSelfBlock(String blockerId, String blockedId) {

        if (blockerId.equals(blockedId)) {
            throw new IllegalArgumentException("User cannot block himself");
        }
    }

    private void validateDuplicateBlock(String blockerId, String blockedId) {

        if (blockedUserRepository.isUserBlocked(blockerId, blockedId)) {
            throw new IllegalArgumentException("User is already blocked");
        }
    }

    private BlockedUser createBlockedUser(String blockerId, String blockedId, String reason) {
        BlockedUser blockedUser = new BlockedUser();
        blockedUser.setBlockerId(blockerId);
        blockedUser.setBlockedId(blockedId);
        blockedUser.setReason(reason);
        return blockedUser;
    }

    private BlockedUser findExistingBlock(String blockerId, String userIdToUnblock) {
        return blockedUserRepository.findByBlockerIdAndBlockedId(blockerId, userIdToUnblock)
                .orElseThrow(() -> new EntityNotFoundException("Blocked user not found"));
    }
}
