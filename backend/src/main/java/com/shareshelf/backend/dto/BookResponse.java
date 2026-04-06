package com.shareshelf.backend.dto;

import java.time.LocalDateTime;

import com.shareshelf.backend.entity.Book.BookCondition;
import com.shareshelf.backend.entity.Book.BookStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookResponse {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String description;
    private String coverImage;
    private BookCondition book_condition;
    private BookStatus book_status;
    private String genre;

    // Owner summary — never expose full User entity
    private Long ownerId;
    private String ownerName;
    
    private Double averageRating;       // null if no reviews yet
    private Long reviewCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
