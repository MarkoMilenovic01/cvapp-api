package com.best.cvapp.admin.user;

import com.best.cvapp.admin.user.dto.AdminUserResponse;
import com.best.cvapp.admin.user.dto.ChangeRoleRequest;
import com.best.cvapp.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<Page<AdminUserResponse>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(adminUserService.getAllUsers(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminUserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.getUserById(id));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<AdminUserResponse> toggleEnabled(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentAdmin) {
        return ResponseEntity.ok(adminUserService.toggleEnabled(id, currentAdmin));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<AdminUserResponse> changeRole(
            @PathVariable Long id,
            @Valid @RequestBody ChangeRoleRequest request,
            @AuthenticationPrincipal User currentAdmin

    ) {
        return ResponseEntity.ok(adminUserService.changeRole(id, request.role(), currentAdmin));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentAdmin) {
        adminUserService.deleteUser(id, currentAdmin);
        return ResponseEntity.noContent().build();
    }
}
