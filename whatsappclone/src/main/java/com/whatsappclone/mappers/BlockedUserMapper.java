package com.whatsappclone.mappers;

import com.whatsappclone.dtos.responses.BlockedUserResponse;
import com.whatsappclone.entities.user.BlockedUser;
import org.springframework.stereotype.Service;

@Service
public class BlockedUserMapper {

    public BlockedUserResponse toBlockedUserResponse(BlockedUser blockedUser) {
        return BlockedUserResponse.builder()
                .id(blockedUser.getId())
                .blockedId(blockedUser.getBlockedId())
                .blockedUserName(blockedUser.getBlocked().getFirstName() + " " +
                        blockedUser.getBlocked().getLastName())
                .blockedUserEmail(blockedUser.getBlocked().getEmail())
                .reason(blockedUser.getReason())
                .blockedAt(blockedUser.getCreatedDate())
                .build();
    }

}
