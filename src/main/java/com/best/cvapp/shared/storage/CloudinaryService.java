package com.best.cvapp.shared.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private static final long MAX_IMAGE_SIZE = 5L  * 1024 * 1024;  // 5 MB
    private static final long MAX_PDF_SIZE   = 10L * 1024 * 1024;  // 10 MB
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final Cloudinary cloudinary;

    // ── CV: profile photo ─────────────────────────────────────────────────────

    public UploadResult uploadCvProfilePhoto(MultipartFile file, Long cvId) {
        validateImage(file);
        String publicId = "cvapp/cvs/" + cvId + "/photo";
        return uploadImage(file, publicId);
    }

    // ── CV: PDF ───────────────────────────────────────────────────────────────

    public UploadResult uploadCvPdf(MultipartFile file, Long cvId) {
        validatePdf(file);
        String publicId = "cvapp/cvs/" + cvId + "/cv_pdf";
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id",     publicId,
                            "resource_type", "raw",
                            "overwrite",     true
                    )
            );
            return new UploadResult(
                    (String) result.get("secure_url"),
                    (String) result.get("public_id")
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload CV PDF: " + e.getMessage(), e);
        }
    }

    // ── Company: photo ────────────────────────────────────────────────────────

    public UploadResult uploadCompanyPhoto(MultipartFile file, Long companyId) {
        validateImage(file);
        String publicId = "cvapp/companies/" + companyId + "/photo";
        return uploadImage(file, publicId);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    public void deleteImage(String publicId) {
        destroy(publicId, "image");
    }

    public void deletePdf(String publicId) {
        destroy(publicId, "raw");
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private UploadResult uploadImage(MultipartFile file, String publicId) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id",     publicId,
                            "resource_type", "image",
                            "overwrite",     true
                    )
            );
            return new UploadResult(
                    (String) result.get("secure_url"),
                    (String) result.get("public_id")
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    private void destroy(String publicId, String resourceType) {
        try {
            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("resource_type", resourceType)
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file from Cloudinary: " + e.getMessage(), e);
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("File must not be empty");
        String ct = file.getContentType();
        if (ct == null || !ALLOWED_IMAGE_TYPES.contains(ct))
            throw new IllegalArgumentException("Only image files are allowed (jpg, png, webp)");
        if (file.getSize() > MAX_IMAGE_SIZE)
            throw new IllegalArgumentException("Image must be smaller than 5 MB");
        try {
            if (!hasAllowedImageSignature(file.getBytes())) {
                throw new IllegalArgumentException("File content is not a valid jpg, png, or webp image");
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to read image file", ex);
        }
    }

    private boolean hasAllowedImageSignature(byte[] bytes) {
        return isJpeg(bytes) || isPng(bytes) || isWebp(bytes);
    }

    private boolean isJpeg(byte[] bytes) {
        return bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF;
    }

    private boolean isPng(byte[] bytes) {
        byte[] signature = {
                (byte) 0x89, 0x50, 0x4E, 0x47,
                0x0D, 0x0A, 0x1A, 0x0A
        };
        if (bytes.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (bytes[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean isWebp(byte[] bytes) {
        return bytes.length >= 12
                && bytes[0] == 'R' && bytes[1] == 'I'
                && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E'
                && bytes[10] == 'B' && bytes[11] == 'P';
    }

    private void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("File must not be empty");
        if (!"application/pdf".equals(file.getContentType()))
            throw new IllegalArgumentException("Only PDF files are allowed");
        if (file.getSize() > MAX_PDF_SIZE)
            throw new IllegalArgumentException("PDF must be smaller than 10 MB");
    }

    // ── Result type ───────────────────────────────────────────────────────────

    public record UploadResult(String url, String publicId) {}
}
