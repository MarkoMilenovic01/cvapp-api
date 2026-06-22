package com.best.cvapp.cv.upload;

import com.best.cvapp.shared.storage.UploadResponse;
import com.best.cvapp.user.User;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user/cv")
@RequiredArgsConstructor
public class CVUploadController {

    private final CVUploadService cvUploadService;

    @PostMapping("/photo")
    public ResponseEntity<UploadResponse> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(cvUploadService.uploadPhoto(file, currentUser));
    }

    @DeleteMapping("/photo")
    public ResponseEntity<Void> deletePhoto(@AuthenticationPrincipal User currentUser) {
        cvUploadService.deletePhoto(currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/pdf")
    public ResponseEntity<UploadResponse> uploadPdf(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(cvUploadService.uploadPdf(file, currentUser));
    }

    @DeleteMapping("/pdf")
    public ResponseEntity<Void> deletePdf(@AuthenticationPrincipal User currentUser) {
        cvUploadService.deletePdf(currentUser);
        return ResponseEntity.noContent().build();
    }
}