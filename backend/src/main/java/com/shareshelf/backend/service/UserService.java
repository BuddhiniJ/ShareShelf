package com.shareshelf.backend.service;

import com.shareshelf.backend.dto.UpdateProfileRequest;
import com.shareshelf.backend.dto.UserProfileResponse;
import com.shareshelf.backend.entity.User;
import com.shareshelf.backend.exception.ResourceNotFoundException;
import com.shareshelf.backend.exception.UnauthorizedException;
import com.shareshelf.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // ── Get current user's own profile ────────────────────────────────────

    public UserProfileResponse getMyProfile() {
        User user = getCurrentUser();
        return mapToProfileResponse(user);
    }

    // ── Update current user's own profile ─────────────────────────────────

    public UserProfileResponse updateMyProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();

        user.setName(request.getName());
        user.setBio(request.getBio());
        user.setProfilePicture(request.getProfilePicture());

        userRepository.save(user);
        return mapToProfileResponse(user);
    }

    // ── Get any user's public profile by ID ───────────────────────────────

    public UserProfileResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToProfileResponse(user);
    }

    // ── Admin: get any profile by ID (same logic, role checked via @PreAuthorize) ──

    public UserProfileResponse getAnyProfile(Long id) {
        User currentUser = getCurrentUser();

        // Allow if admin OR if requesting own profile
        boolean isAdmin = currentUser.getRole().name().equals("ROLE_ADMIN");
        boolean isSelf  = currentUser.getId().equals(id);

        if (!isAdmin && !isSelf) {
            throw new UnauthorizedException("You are not allowed to access this profile");
        }

        User target = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        return mapToProfileResponse(target);
    }

    // ── Shared helper: extract current user from SecurityContext ──────────

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    // ── Shared mapper: User entity → UserProfileResponse DTO ─────────────

    private UserProfileResponse mapToProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .bio(user.getBio())
                .profilePicture(user.getProfilePicture())
                .role(user.getRole().name())
                .build();
    }
}