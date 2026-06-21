package com.best.cvapp.company.cv;

import com.best.cvapp.company.cv.dto.CompanyCVSummaryResponse;
import com.best.cvapp.cv.CVResponse;
import com.best.cvapp.cv.CVSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company/cvs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COMPANY')")
public class CompanyCVController {

    private final CompanyCVService companyCVService;


    @GetMapping
    public ResponseEntity<Page<CompanyCVSummaryResponse>> getAllCVs(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(companyCVService.getAllCVs(pageable));
    }



    @GetMapping("/search")
    public ResponseEntity<Page<CompanyCVSummaryResponse>> searchCVs(
            CVSearchRequest request,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(companyCVService.searchCVs(request, pageable));
    }


    @GetMapping("/{id}")
    public ResponseEntity<CVResponse> getCVById(@PathVariable Long id) {
        return ResponseEntity.ok(companyCVService.getCVById(id));
    }
}