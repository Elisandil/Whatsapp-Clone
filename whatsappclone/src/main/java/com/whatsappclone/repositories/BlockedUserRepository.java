package com.whatsappclone.repositories;

import com.whatsappclone.entities.user.BlockedUser;
import com.whatsappclone.entities.user.BlockedUserConstants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BlockedUserRepository extends JpaRepository<BlockedUser, String> {

    @Query(name = BlockedUserConstants.FIND_BLOCKED_USERS_BY_BLOCKER_ID)
    List<BlockedUser> findBlockedUsersByBlockerId(@Param("blockerId") String blockerId);

    @Query(name = BlockedUserConstants.IS_USER_BLOCKED)
    boolean isUserBlocked(@Param("blockerId") String blockerId, @Param("blockedId") String blockedId);

    @Query(name = BlockedUserConstants.FIND_MUTUAL_BLOCK_STATUS)
    List<BlockedUser> findMutualBlockStatus(@Param("userId1") String userId1, @Param("userId2") String userId2);

    Optional<BlockedUser> findByBlockerIdAndBlockedId(String blockerId, String blockedId);
}