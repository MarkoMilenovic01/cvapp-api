package com.best.cvapp.company.profile;

import com.best.cvapp.company.profile.dto.CompanyRequest;
import com.best.cvapp.company.profile.dto.CompanyResponse;
import com.best.cvapp.company.profile.exception.CompanyAccountNotFoundException;
import com.best.cvapp.company.profile.exception.CompanyAuthenticationRequiredException;
import com.best.cvapp.company.profile.exception.CompanyUserNotFoundException;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles the authenticated company's profile.
 *
 * Flow:
 * 1. Resolve the authenticated user from the security context.
 * 2. Load the company profile belonging to that user.
 * 3. Return the current profile or update its submitted details.
 * 4. Save profile changes.
 * 5. Map the company to its response.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyProfileService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public CompanyResponse getMyProfile() {
        Company company = getAuthenticatedCompany();
        return mapToResponse(company);
    }

    @Transactional
    public CompanyResponse updateMyProfile(CompanyRequest request) {
        Company company = getAuthenticatedCompany();

        company.setName(request.name());
        company.setDescription(request.description());
        company.setWebsite(request.website());
        company.setIndustry(request.industry());

        Company savedCompany = companyRepository.save(company);

        return mapToResponse(savedCompany);
    }

    public Company getAuthenticatedCompany() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CompanyAuthenticationRequiredException();
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(CompanyUserNotFoundException::new);

        return companyRepository.findByUser(user)
                .orElseThrow(CompanyAccountNotFoundException::new);
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
