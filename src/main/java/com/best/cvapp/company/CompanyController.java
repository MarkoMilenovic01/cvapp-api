package com.best.cvapp.company;

import com.best.cvapp.company.dto.*;
import com.best.cvapp.cv.CVResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COMPANY')")
public class CompanyController {

    private final CompanyService companyService;

    // --- Profile ---

    @GetMapping("/me")
    public ResponseEntity<CompanyResponse> getMyProfile() {
        return ResponseEntity.ok(companyService.getMyProfile());
    }

    @PutMapping("/me")
    public ResponseEntity<CompanyResponse> updateMyProfile(@RequestBody CompanyRequest request) {
        return ResponseEntity.ok(companyService.updateMyProfile(request));
    }

    // --- CV Browsing ---

    @GetMapping("/cvs")
    public ResponseEntity<Page<CvSummaryResponse>> getAllCVs(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(companyService.getAllCVs(pageable));
    }

    @GetMapping("/cvs/{id}")
    public ResponseEntity<CVResponse> getCVById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getCVById(id));
    }

    // --- Favorites ---

    @PostMapping("/cvs/{id}/favorite")
    public ResponseEntity<Void> addFavorite(@PathVariable Long id) {
        companyService.addFavorite(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cvs/{id}/favorite")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long id) {
        companyService.removeFavorite(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<CvSummaryResponse>> getFavorites() {
        return ResponseEntity.ok(companyService.getFavorites());
    }

    // --- View History ---

    @GetMapping("/history")
    public ResponseEntity<List<CvViewResponse>> getViewHistory() {
        return ResponseEntity.ok(companyService.getViewHistory());
    }
}