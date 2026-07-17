package com.best.cvapp.company.profile;

import com.best.cvapp.company.profile.dto.CompanyRequest;
import com.best.cvapp.company.profile.dto.CompanyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company/me")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COMPANY')")
public class CompanyProfileController {

    private final CompanyProfileService companyProfileService;

    @GetMapping
    public ResponseEntity<CompanyResponse> getMyProfile() {
        return ResponseEntity.ok(companyProfileService.getMyProfile());
    }

    @PutMapping
    public ResponseEntity<CompanyResponse> updateMyProfile(
            @Valid @RequestBody CompanyRequest request
    ) {
        return ResponseEntity.ok(companyProfileService.updateMyProfile(request));
    }
}
