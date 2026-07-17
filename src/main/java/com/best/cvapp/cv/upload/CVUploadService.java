package com.best.cvapp.cv.upload;

import com.best.cvapp.cv.profile.CV;
import com.best.cvapp.cv.profile.CVRepository;
import com.best.cvapp.cv.profile.exception.CVNotFoundException;
import com.best.cvapp.shared.storage.CloudinaryService;
import com.best.cvapp.shared.storage.UploadResponse;
import com.best.cvapp.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Handles profile photo and PDF uploads for the authenticated user's CV.
 *
 * Flow:
 * 1. Load the CV belonging to the authenticated user.
 * 2. Upload the photo or PDF to its deterministic Cloudinary location.
 * 3. Store the uploaded file's URL and public ID on the CV.
 * 4. Clear the stored file details when the user removes a file.
 * 5. Delete removed files from Cloudinary.
 */
@Service
@RequiredArgsConstructor
public class CVUploadService {

    private final CloudinaryService cloudinaryService;
    private final CVRepository cvRepository;

    public UploadResponse uploadPhoto(
            MultipartFile file,
            User currentUser
    ) {
        CV cv = getUserCV(currentUser);

        CloudinaryService.UploadResult result =
                cloudinaryService.uploadCvProfilePhoto(file, cv.getId());
        cv.setProfilePhotoUrl(result.url());
        cv.setProfilePhotoId(result.publicId());
        cvRepository.save(cv);

        return new UploadResponse(result.url());
    }

    public void deletePhoto(User currentUser) {
        CV cv = getUserCV(currentUser);
        String photoId = cv.getProfilePhotoId();
        if (photoId == null) {
            return;
        }
        cv.setProfilePhotoUrl(null);
        cv.setProfilePhotoId(null);
        cvRepository.save(cv);
        cloudinaryService.deleteImage(photoId);
    }

    public UploadResponse uploadPdf(
            MultipartFile file,
            User currentUser
    ) {
        CV cv = getUserCV(currentUser);

        CloudinaryService.UploadResult result =
                cloudinaryService.uploadCvPdf(file, cv.getId());
        cv.setPdfUrl(result.url());
        cv.setPdfPublicId(result.publicId());
        cvRepository.save(cv);

        return new UploadResponse(result.url());
    }

    public void deletePdf(User currentUser) {
        CV cv = getUserCV(currentUser);
        String pdfPublicId = cv.getPdfPublicId();
        if (pdfPublicId == null) {
            return;
        }
        cv.setPdfUrl(null);
        cv.setPdfPublicId(null);
        cvRepository.save(cv);
        cloudinaryService.deletePdf(pdfPublicId);
    }

    private CV getUserCV(User currentUser) {
        return cvRepository.findByUser(currentUser)
                .orElseThrow(CVNotFoundException::new);
    }
}
