package com.shareshelf.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlatformStatsResponse {

    // User stats
    private long totalUsers;
    private long totalAdmins;

    // Book stats
    private long totalBooks;
    private long availableBooks;
    private long borrowedBooks;

    // Borrow stats
    private long totalBorrowRequests;
    private long pendingRequests;
    private long approvedRequests;
    private long returnedRequests;
    private long rejectedRequests;

    // Review stats
    private long totalReviews;
    private double averageRatingPlatform;

    // Notification stats
    private long totalUnreadNotifications;
}