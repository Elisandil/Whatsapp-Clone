package com.whatsappclone.dtos.requests;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupRequest {
    private String groupName;
    private String description;
    private List<String> memberIds;

}
