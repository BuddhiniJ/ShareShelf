package com.shareshelf.backend.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewResponse {
	
    private Long id;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Reviewer info
    private Long reviewerId;
    private String reviewerName;

    // Book info
    private Long bookId;
    private String bookTitle;

    // Borrow reference
    private Long borrowRequestId;

}
