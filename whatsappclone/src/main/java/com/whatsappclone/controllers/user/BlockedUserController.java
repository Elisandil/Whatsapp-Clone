package com.whatsappclone.controllers.user;

import com.whatsappclone.dtos.requests.BlockedUserRequest;
import com.whatsappclone.dtos.responses.BlockedUserResponse;
import com.whatsappclone.services.user.BlockedUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/blocked-users")
@RequiredArgsConstructor
@Tag(name = "Blocked Users", description = "Blocked User management")
public class BlockedUserController {
    private final BlockedUserService blockedUserService;

    // ------------------- POST METHODS ---------------------------------------
    @PostMapping("/block")
    @Operation(summary = "Block users", description = "Blocking target user")
    public ResponseEntity<String> blockUser(
            Authentication auth,
            @Valid @RequestBody BlockedUserRequest request) {
        blockedUserService.blockUser(auth, request);
        return ResponseEntity.ok("User blocked successfully");
    }

    // ------------------- DELETE METHODS ---------------------------------------
    @DeleteMapping("/unblock/{userId}")
    @Operation(summary = "Unblock user", description = "Unblock target user")
    public ResponseEntity<String> unblockUser(
            Authentication auth,
            @PathVariable String userId) {
        blockedUserService.unblockUser(auth, userId);
        return ResponseEntity.ok("Unblocked user");
    }

    // ------------------- GET METHODS ---------------------------------------
    @GetMapping
    @Operation(summary = "List of blocked users", description = "List of blocked users")
    public ResponseEntity<List<BlockedUserResponse>> getBlockedUsers(Authentication auth) {
        return ResponseEntity.ok(blockedUserService.getBlockedUsers(auth));
    }

    @GetMapping("/is-blocked/{userId}")
    @Operation(summary = "Verify if the user is blocked", description = "Verify if the user is blocked")
    public ResponseEntity<Boolean> isUserBlocked(Authentication auth, @PathVariable String userId) {
        boolean isBlocked = blockedUserService.isUserBlocked(auth.getName(), userId);
        return ResponseEntity.ok(isBlocked);
    }
}