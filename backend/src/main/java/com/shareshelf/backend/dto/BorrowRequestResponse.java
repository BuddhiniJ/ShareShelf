package com.shareshelf.backend.dto;

import java.time.LocalDateTime;

import com.shareshelf.backend.entity.BorrowRequest.BorrowStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BorrowRequestResponse {

    private Long id;
    private BorrowStatus status;
    private String message;
    private String ownerNote;
    private LocalDateTime requestedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime dueDate;

    // Book summary
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;

    // Borrower summary
    private Long borrowerId;
    private String borrowerName;

    // Owner summary
    private Long ownerId;
    private String ownerName;
}
