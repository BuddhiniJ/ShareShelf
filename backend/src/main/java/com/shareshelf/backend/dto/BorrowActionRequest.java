package com.shareshelf.backend.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BorrowActionRequest {

	@Size(max = 500, message = "Note must not exceed 500 characters")
    private String ownerNote;       // optional message from owner

    private LocalDateTime dueDate;  // required when approving
	
}
