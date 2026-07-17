package com.best.cvapp.admin.user.dto;

import com.best.cvapp.user.Role;
import jakarta.validation.constraints.NotNull;


public record ChangeRoleRequest(
        @NotNull Role role
) {}