package com.best.cvapp.cv.education;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/cv/education")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class EducationController {

    private final EducationService educationService;

    @GetMapping
    public ResponseEntity<List<EducationResponse>> getAll() {
        return ResponseEntity.ok(educationService.getAll());
    }

    @PostMapping
    public ResponseEntity<EducationResponse> add(@RequestBody EducationRequest request) {
        return ResponseEntity.ok(educationService.add(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EducationResponse> update(@PathVariable Long id, @RequestBody EducationRequest request) {
        return ResponseEntity.ok(educationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        educationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}