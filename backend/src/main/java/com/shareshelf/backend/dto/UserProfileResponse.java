package com.shareshelf.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {

    private Long id;
    private String name;
    private String email;
    private String bio;
    private String profilePicture;
    private String role;

}