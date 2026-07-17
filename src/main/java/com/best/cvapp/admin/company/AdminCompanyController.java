package com.best.cvapp.admin.company;

import com.best.cvapp.admin.AdminPageSizeValidator;
import com.best.cvapp.admin.company.dto.AdminCompanyResponse;
import com.best.cvapp.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/companies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCompanyController {

    private final AdminCompanyService adminCompanyService;

    @GetMapping
    public ResponseEntity<Page<AdminCompanyResponse>> getAllCompanies(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        AdminPageSizeValidator.validate(pageable);
        return ResponseEntity.ok(adminCompanyService.getAllCompanies(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminCompanyResponse> getCompanyById(@PathVariable Long id) {
        return ResponseEntity.ok(adminCompanyService.getCompanyById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentAdmin) {
        adminCompanyService.deleteCompany(id, currentAdmin);
        return ResponseEntity.noContent().build();
    }
}
