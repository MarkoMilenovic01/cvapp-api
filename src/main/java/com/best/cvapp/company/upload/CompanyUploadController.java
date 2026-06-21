package com.best.cvapp.company.upload;

import com.best.cvapp.shared.storage.UploadResponse;
import com.best.cvapp.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COMPANY')")
public class CompanyUploadController {

    private final CompanyUploadService companyUploadService;

    @PostMapping("/photo")
    public ResponseEntity<UploadResponse> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(
                companyUploadService.uploadPhoto(file, currentUser)
        );
    }

    @DeleteMapping("/photo")
    public ResponseEntity<Void> deletePhoto(
            @AuthenticationPrincipal User currentUser
    ) {
        companyUploadService.deletePhoto(currentUser);
        return ResponseEntity.noContent().build();
    }
}