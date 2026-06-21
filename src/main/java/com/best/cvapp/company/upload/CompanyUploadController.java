package com.best.cvapp.company;

import com.best.cvapp.company.profile.Company;
import com.best.cvapp.company.profile.CompanyRepository;
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
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyUploadController {

    private final CloudinaryService cloudinaryService;
    private final CompanyRepository companyRepository;

    /**
     * POST /api/company/photo
     * Upload or replace the company photo.
     */
    @PostMapping("/photo")
    public ResponseEntity<UploadResponse> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User currentUser
    ) {
        Company company = getCompanyForUser(currentUser);

        // Delete old photo from Cloudinary if one exists
        if (company.getPhotoPublicId() != null) {
            cloudinaryService.deleteImage(company.getPhotoPublicId());
        }

        CloudinaryService.UploadResult result = cloudinaryService.uploadCompanyPhoto(file, company.getId());

        company.setPhotoUrl(result.url());
        company.setPhotoPublicId(result.publicId());
        companyRepository.save(company);

        return ResponseEntity.ok(new UploadResponse(result.url()));
    }

    /**
     * DELETE /api/company/photo
     * Remove the company photo from Cloudinary and clear it.
     */
    @DeleteMapping("/photo")
    public ResponseEntity<Void> deletePhoto(
            @AuthenticationPrincipal User currentUser
    ) {
        Company company = getCompanyForUser(currentUser);

        if (company.getPhotoPublicId() != null) {
            cloudinaryService.deleteImage(company.getPhotoPublicId());
            company.setPhotoUrl(null);
            company.setPhotoPublicId(null);
            companyRepository.save(company);
        }

        return ResponseEntity.noContent().build();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Company getCompanyForUser(User user) {
        return companyRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Company not found"));
    }
}