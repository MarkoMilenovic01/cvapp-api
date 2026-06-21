package com.best.cvapp.cv.upload;

import com.best.cvapp.cv.profile.CV;
import com.best.cvapp.cv.profile.CVRepository;
import com.best.cvapp.shared.storage.CloudinaryService;
import com.best.cvapp.shared.storage.UploadResponse;
import com.best.cvapp.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CVUploadService {

    private final CloudinaryService cloudinaryService;
    private final CVRepository cvRepository;

    // ── Profile photo ─────────────────────────────────────────────────────────

    public UploadResponse uploadPhoto(MultipartFile file, User currentUser) {
        CV cv = getCvForUser(currentUser);

        if (cv.getProfilePhotoId() != null) {
            cloudinaryService.deleteImage(cv.getProfilePhotoId());
        }

        CloudinaryService.UploadResult result = cloudinaryService.uploadCvProfilePhoto(file, cv.getId());

        cv.setProfilePhotoUrl(result.url());
        cv.setProfilePhotoId(result.publicId());
        cvRepository.save(cv);

        return new UploadResponse(result.url());
    }

    public void deletePhoto(User currentUser) {
        CV cv = getCvForUser(currentUser);

        if (cv.getProfilePhotoId() != null) {
            cloudinaryService.deleteImage(cv.getProfilePhotoId());
            cv.setProfilePhotoUrl(null);
            cv.setProfilePhotoId(null);
            cvRepository.save(cv);
        }
    }

    // ── PDF ───────────────────────────────────────────────────────────────────

    public UploadResponse uploadPdf(MultipartFile file, User currentUser) {
        CV cv = getCvForUser(currentUser);

        if (cv.getPdfPublicId() != null) {
            cloudinaryService.deletePdf(cv.getPdfPublicId());
        }

        CloudinaryService.UploadResult result = cloudinaryService.uploadCvPdf(file, cv.getId());

        cv.setPdfUrl(result.url());
        cv.setPdfPublicId(result.publicId());
        cvRepository.save(cv);

        return new UploadResponse(result.url());
    }

    public void deletePdf(User currentUser) {
        CV cv = getCvForUser(currentUser);

        if (cv.getPdfPublicId() != null) {
            cloudinaryService.deletePdf(cv.getPdfPublicId());
            cv.setPdfUrl(null);
            cv.setPdfPublicId(null);
            cvRepository.save(cv);
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private CV getCvForUser(User user) {
        return cvRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "CV not found — create your CV first"));
    }
}