package com.whatsappclone.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlockedUserRequest {
    @NotBlank(message = "User ID to block is required")
    private String userIdToBlock;

    private String reason;
}

