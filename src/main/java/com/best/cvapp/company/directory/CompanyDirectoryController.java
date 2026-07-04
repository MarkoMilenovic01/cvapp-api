package com.best.cvapp.company.directory;

import com.best.cvapp.company.profile.dto.CompanyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class CompanyDirectoryController {

    private final CompanyDirectoryService companyDirectoryService;

    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyResponse> getCompanyById(
            @PathVariable Long companyId
    ) {
        return ResponseEntity.ok(companyDirectoryService.getCompanyById(companyId));
    }
}