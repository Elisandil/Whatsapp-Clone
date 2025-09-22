package com.whatsappclone.controllers;

import com.whatsappclone.dtos.requests.GroupRequest;
import com.whatsappclone.dtos.responses.GroupResponse;
import com.whatsappclone.dtos.responses.StringResponse;
import com.whatsappclone.services.GroupService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Tag(name = "Group")
public class GroupController {
    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<StringResponse> createGroup(
            @RequestBody GroupRequest request,
            Authentication auth) {
        String groupId = groupService.createGroup(request, auth);
        return ResponseEntity.ok(StringResponse.builder().message(groupId).build());
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getUserGroups(Authentication auth) {
        return ResponseEntity.ok(groupService.getUserGroups(auth));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<Void> addMemberToGroup(
            @PathVariable String groupId,
            @RequestParam String userId,
            Authentication auth) {
        groupService.addMemberToGroup(groupId, userId, auth);
        return ResponseEntity.ok().build();
    }
}

