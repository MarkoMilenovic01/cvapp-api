package com.best.cvapp.admin.company;

import com.best.cvapp.admin.company.dto.AdminCompanyResponse;
import com.best.cvapp.admin.company.exception.AdminCompanyNotFoundException;
import com.best.cvapp.admin.user.AdminUserService;
import com.best.cvapp.company.profile.Company;
import com.best.cvapp.company.profile.CompanyRepository;
import com.best.cvapp.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles administrator access to company accounts.
 *
 * Flow:
 * 1. Load all companies with pagination or find one company by ID.
 * 2. Map company and owner details to an admin response.
 * 3. Delegate company deletion to the shared administrator user-deletion flow.
 * 4. Remove the owner, company data, dependent records, and stored files together.
 */
@Service
@RequiredArgsConstructor
public class AdminCompanyService {

    private final CompanyRepository companyRepository;
    private final AdminUserService adminUserService;

    @Transactional(readOnly = true)
    public Page<AdminCompanyResponse> getAllCompanies(Pageable pageable) {
        return companyRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AdminCompanyResponse getCompanyById(Long id) {
        return toResponse(findCompany(id));
    }

    @Transactional
    public void deleteCompany(Long id, User currentAdmin) {
        Company company = findCompany(id);
        adminUserService.deleteUser(company.getUser().getId(), currentAdmin);
    }

    private Company findCompany(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(AdminCompanyNotFoundException::new);
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
