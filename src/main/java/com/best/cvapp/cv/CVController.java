package com.best.cvapp.cv;

import com.best.cvapp.cv.dto.CVRequest;
import com.best.cvapp.cv.dto.CVResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/cv")
@RequiredArgsConstructor
public class CVController {

    private final CVService cvService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CVResponse> getMyCV() {
        return ResponseEntity.ok(cvService.getMyCV());
    }

    @PutMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CVResponse> createOrUpdateCV(@RequestBody CVRequest request) {
        return ResponseEntity.ok(cvService.createOrUpdateCV(request));
    }

    @DeleteMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteCV() {
        cvService.deleteCV();
        return ResponseEntity.noContent().build();
    }
}