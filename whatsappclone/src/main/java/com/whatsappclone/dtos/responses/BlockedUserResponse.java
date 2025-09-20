package com.whatsappclone.dtos.responses;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockedUserResponse {
    private String id;
    private String blockedId;
    private String blockedUserName;
    private String blockedUserEmail;
    private String reason;
    private LocalDateTime blockedAt;
}

