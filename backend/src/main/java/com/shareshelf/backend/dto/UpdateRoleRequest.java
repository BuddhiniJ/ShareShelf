package com.shareshelf.backend.dto;

import com.shareshelf.backend.entity.User.Role;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRoleRequest {

    @NotNull(message = "Role is required")
    private Role role;
}