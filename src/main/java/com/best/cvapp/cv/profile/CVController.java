package com.best.cvapp.cv.profile;

import com.best.cvapp.cv.profile.dto.CVRequest;
import com.best.cvapp.cv.profile.dto.CVResponse;
import com.best.cvapp.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/cv")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class CVController {

    private final CVService cvService;

    @GetMapping
    public ResponseEntity<CVResponse> getMyCV(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(cvService.getMyCV(currentUser));
    }

    @PutMapping
    public ResponseEntity<CVResponse> createOrUpdateCV(
            @Valid @RequestBody CVRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(
                cvService.createOrUpdateCV(request, currentUser)
        );
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteCV(
            @AuthenticationPrincipal User currentUser
    ) {
        cvService.deleteCV(currentUser);
        return ResponseEntity.noContent().build();
    }
}