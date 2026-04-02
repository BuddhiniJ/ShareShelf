package com.shareshelf.backend.dto;

import com.shareshelf.backend.entity.Book.BookCondition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    private String isbn;                // optional

    private String description;        // optional

    private String coverImage;         // optional — URL

    @NotNull(message = "Condition is required")
    private BookCondition condition;

    @NotBlank(message = "Genre is required")
    private String genre;
}