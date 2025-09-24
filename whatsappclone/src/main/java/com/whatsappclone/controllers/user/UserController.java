package com.whatsappclone.controllers.user;

import com.whatsappclone.dtos.responses.UserResponse;
import com.whatsappclone.services.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User")
public class UserController {
    private final UserService userService;

    // ------------------- GET METHODS ---------------------------------------
    @GetMapping
    @Operation(summary = "Get all users", description = "Get all users, not counting the blocked ones")
    public ResponseEntity<List<UserResponse>> getAllUsers(Authentication auth) {
        return ResponseEntity.ok(userService.getAllUsersExceptSelf(auth));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all users without filters", description = "Get all users without filters")
    public ResponseEntity<List<UserResponse>> getAllUsersWithoutFilter(Authentication auth) {
        return ResponseEntity.ok(userService.getAllUsersExceptSelfAndBlocked(auth));
    }
}