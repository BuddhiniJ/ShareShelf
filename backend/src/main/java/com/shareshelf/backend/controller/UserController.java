package com.shareshelf.backend.controller;

import com.shareshelf.backend.dto.UpdateProfileRequest;
import com.shareshelf.backend.dto.UserProfileResponse;
import com.shareshelf.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET /api/users/me — logged-in user fetches their own profile
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    // PUT /api/users/me — logged-in user updates their own profile
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateMyProfile(request));
    }

    // GET /api/users/{id} — view any user's public profile
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // GET /api/users/{id}/admin — admin-only full profile access
    @GetMapping("/{id}/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserProfileResponse> getAnyProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getAnyProfile(id));
    }
}