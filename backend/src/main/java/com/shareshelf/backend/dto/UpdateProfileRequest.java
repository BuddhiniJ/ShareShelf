package com.shareshelf.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Full name is required")
    private String name;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;                 // optional — can be null/blank

    private String profilePicture;     // optional — URL string
}