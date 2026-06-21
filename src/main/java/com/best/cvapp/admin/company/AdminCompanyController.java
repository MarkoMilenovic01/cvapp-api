package com.best.cvapp.admin.company;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/companies")
@RequiredArgsConstructor
public class AdminCompanyController {

    private final AdminCompanyService adminCompanyService;

    @GetMapping
    public ResponseEntity<Page<AdminCompanyResponse>> getAllCompanies(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(adminCompanyService.getAllCompanies(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminCompanyResponse> getCompanyById(@PathVariable Long id) {
        return ResponseEntity.ok(adminCompanyService.getCompanyById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        adminCompanyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }
}