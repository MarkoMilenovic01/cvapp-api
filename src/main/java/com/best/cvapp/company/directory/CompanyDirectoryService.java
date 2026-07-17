package com.best.cvapp.company.directory;

import com.best.cvapp.company.profile.Company;
import com.best.cvapp.company.profile.CompanyRepository;
import com.best.cvapp.company.profile.dto.CompanyResponse;
import com.best.cvapp.company.directory.exception.DirectoryCompanyNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles public company-directory lookups for authenticated users.
 *
 * Flow:
 * 1. Load the requested company by its identifier.
 * 2. Reject the request when the company does not exist.
 * 3. Map the company profile to its public response.
 * 4. Return the company details.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyDirectoryService {

    private final CompanyRepository companyRepository;

    public CompanyResponse getCompanyById(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(DirectoryCompanyNotFoundException::new);

        return mapToResponse(company);
    }

    private CompanyResponse mapToResponse(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getDescription(),
                company.getWebsite(),
                company.getIndustry(),
                company.getPhotoUrl(),
                company.getCreatedAt()
        );
    }
}
