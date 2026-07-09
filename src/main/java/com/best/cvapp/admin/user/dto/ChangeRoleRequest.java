package com.best.cvapp.admin.user.dto;

import com.best.cvapp.user.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeRoleRequest {
    private Role role;
}