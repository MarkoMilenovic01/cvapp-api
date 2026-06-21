package com.best.cvapp.company.upload;

import com.best.cvapp.company.profile.Company;
import com.best.cvapp.company.profile.CompanyRepository;
import com.best.cvapp.shared.storage.CloudinaryService;
import com.best.cvapp.shared.storage.UploadResponse;
import com.best.cvapp.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CompanyUploadService {

    private final CloudinaryService cloudinaryService;
    private final CompanyRepository companyRepository;

    @Transactional
    public UploadResponse uploadPhoto(MultipartFile file, User currentUser) {
        Company company = getCompanyForUser(currentUser);

        if (company.getPhotoPublicId() != null) {
            cloudinaryService.deleteImage(company.getPhotoPublicId());
        }

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
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Company not found"
                ));
    }
}