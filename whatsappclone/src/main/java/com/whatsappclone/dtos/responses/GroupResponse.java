package com.whatsappclone.dtos.responses;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupResponse {
    private String groupId;
    private String groupName;
    private String description;
    private String groupPhoto;
    private String createdByName;
    private int memberCount;
    private List<String> memberNames;
    private LocalDateTime createdAt;
    private boolean isUserAdmin;

}
