package com.best.cvapp.admin.company;

import com.best.cvapp.admin.company.dto.AdminCompanyResponse;
import com.best.cvapp.company.profile.Company;
import com.best.cvapp.company.profile.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCompanyService {

    private final CompanyRepository companyRepository;

    public Page<AdminCompanyResponse> getAllCompanies(Pageable pageable) {
        return companyRepository.findAll(pageable)
                .map(this::toResponse);
    }

    public AdminCompanyResponse getCompanyById(Long id) {
        return toResponse(findCompany(id));
    }

    @Transactional
    public void deleteCompany(Long id) {
        companyRepository.delete(findCompany(id));
    }

    private Company findCompany(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Company not found"));
    }

    private AdminCompanyResponse toResponse(Company company) {
        return new AdminCompanyResponse(
                company.getId(),
                company.getUser().getId(),
                company.getUser().getEmail(),
                company.getName(),
                company.getDescription(),
                company.getWebsite(),
                company.getIndustry(),
                company.getPhotoUrl(),
                company.getCreatedAt()
        );
    }
}