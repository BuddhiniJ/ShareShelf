package com.shareshelf.backend.dto;

import com.shareshelf.backend.entity.User.Role;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminUserResponse {

    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private String bio;
    private String profilePicture;

    // Aggregated stats per user
    private long totalBooksListed;
    private long totalBorrowsMade;
    private long totalBorrowsReceived;
    private long totalReviewsWritten;
}