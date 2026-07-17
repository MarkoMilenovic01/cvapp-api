package com.best.cvapp.cv.education;

import com.best.cvapp.cv.education.dto.EducationRequest;
import com.best.cvapp.cv.education.dto.EducationResponse;
import com.best.cvapp.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/cv/education")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class EducationController {

    private final EducationService educationService;

    @GetMapping
    public ResponseEntity<List<EducationResponse>> getAll(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(educationService.getAll(currentUser));
    }

    @PostMapping
    public ResponseEntity<EducationResponse> add(
            @Valid @RequestBody EducationRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        EducationResponse response = educationService.add(request, currentUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EducationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EducationRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(
                educationService.update(id, request, currentUser)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        educationService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}