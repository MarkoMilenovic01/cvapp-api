package com.best.cvapp.cv;

import com.best.cvapp.shared.storage.CloudinaryService;
import com.best.cvapp.shared.storage.UploadResponse;
import com.best.cvapp.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/user/cv")
@RequiredArgsConstructor
public class CVUploadController {

    private final CloudinaryService cloudinaryService;
    private final CVRepository      cvRepository;

    // ── Profile photo ─────────────────────────────────────────────────────────

    /**
     * POST /api/user/cv/photo
     * Upload or replace the user's profile photo on their CV.
     */
    @PostMapping("/photo")
    public ResponseEntity<UploadResponse> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User currentUser
    ) {
        CV cv = getCvForUser(currentUser);

        // Delete old photo from Cloudinary if one exists
        if (cv.getProfilePhotoId() != null) {
            cloudinaryService.deleteImage(cv.getProfilePhotoId());
        }

        CloudinaryService.UploadResult result = cloudinaryService.uploadCvProfilePhoto(file, cv.getId());

        cv.setProfilePhotoUrl(result.url());
        cv.setProfilePhotoId(result.publicId());
        cvRepository.save(cv);

        return ResponseEntity.ok(new UploadResponse(result.url()));
    }

    /**
     * DELETE /api/user/cv/photo
     * Remove the profile photo from Cloudinary and clear it from the CV.
     */
    @DeleteMapping("/photo")
    public ResponseEntity<Void> deletePhoto(
            @AuthenticationPrincipal User currentUser
    ) {
        CV cv = getCvForUser(currentUser);

        if (cv.getProfilePhotoId() != null) {
            cloudinaryService.deleteImage(cv.getProfilePhotoId());
            cv.setProfilePhotoUrl(null);
            cv.setProfilePhotoId(null);
            cvRepository.save(cv);
        }

        return ResponseEntity.noContent().build();
    }

    // ── PDF ───────────────────────────────────────────────────────────────────

    /**
     * POST /api/user/cv/pdf
     * Upload or replace the custom PDF for the user's CV.
     */
    @PostMapping("/pdf")
    public ResponseEntity<UploadResponse> uploadPdf(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User currentUser
    ) {
        CV cv = getCvForUser(currentUser);

        // Delete old PDF from Cloudinary if one exists
        if (cv.getPdfPublicId() != null) {
            cloudinaryService.deletePdf(cv.getPdfPublicId());
        }

        CloudinaryService.UploadResult result = cloudinaryService.uploadCvPdf(file, cv.getId());

        cv.setPdfUrl(result.url());
        cv.setPdfPublicId(result.publicId());
        cvRepository.save(cv);

        return ResponseEntity.ok(new UploadResponse(result.url()));
    }

    /**
     * DELETE /api/user/cv/pdf
     * Remove the PDF from Cloudinary and clear it from the CV.
     */
    @DeleteMapping("/pdf")
    public ResponseEntity<Void> deletePdf(
            @AuthenticationPrincipal User currentUser
    ) {
        CV cv = getCvForUser(currentUser);

        if (cv.getPdfPublicId() != null) {
            cloudinaryService.deletePdf(cv.getPdfPublicId());
            cv.setPdfUrl(null);
            cv.setPdfPublicId(null);
            cvRepository.save(cv);
        }

        return ResponseEntity.noContent().build();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private CV getCvForUser(User user) {
        return cvRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "CV not found — create your CV first"));
    }
}