package com.best.cvapp.company.profile;

import com.best.cvapp.company.profile.dto.CompanyRequest;
import com.best.cvapp.company.profile.dto.CompanyResponse;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Unauthenticated"
            );
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        return companyRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Company not found"
                ));
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