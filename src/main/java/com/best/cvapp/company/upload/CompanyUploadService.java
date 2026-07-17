package com.best.cvapp.company.upload;

import com.best.cvapp.company.profile.Company;
import com.best.cvapp.company.profile.CompanyRepository;
import com.best.cvapp.company.upload.exception.UploadCompanyNotFoundException;
import com.best.cvapp.shared.storage.CloudinaryService;
import com.best.cvapp.shared.storage.UploadResponse;
import com.best.cvapp.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Handles company profile-photo uploads for the authenticated company.
 *
 * Flow:
 * 1. Load the company belonging to the authenticated user.
 * 2. Validate and upload a photo to the company's Cloudinary location.
 * 3. Store the uploaded photo URL and public identifier.
 * 4. Delete the stored photo from Cloudinary when requested.
 * 5. Clear the company's saved photo details.
 */
@Service
@RequiredArgsConstructor
public class CompanyUploadService {

    private final CloudinaryService cloudinaryService;
    private final CompanyRepository companyRepository;

    @Transactional
    public UploadResponse uploadPhoto(MultipartFile file, User currentUser) {
        Company company = getCompanyForUser(currentUser);

        CloudinaryService.UploadResult result =
                cloudinaryService.uploadCompanyPhoto(file, company.getId());

        company.setPhotoUrl(result.url());
        company.setPhotoPublicId(result.publicId());

        companyRepository.save(company);

        return new UploadResponse(result.url());
    }

    @Transactional
    public void deletePhoto(User currentUser) {
        Company company = getCompanyForUser(currentUser);

        if (company.getPhotoPublicId() == null) {
            return;
        }

        cloudinaryService.deleteImage(company.getPhotoPublicId());

        company.setPhotoUrl(null);
        company.setPhotoPublicId(null);

        companyRepository.save(company);
    }

    private Company getCompanyForUser(User user) {
        return companyRepository.findByUser(user)
                .orElseThrow(UploadCompanyNotFoundException::new);
    }
}
