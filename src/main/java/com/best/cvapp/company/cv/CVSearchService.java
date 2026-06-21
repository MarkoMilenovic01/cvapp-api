package com.best.cvapp.company.cv;

import com.best.cvapp.company.cv.dto.CVSearchRequest;
import com.best.cvapp.company.profile.Company;
import com.best.cvapp.company.profile.CompanyRepository;
import com.best.cvapp.company.favorite.FavoriteCVId;
import com.best.cvapp.company.favorite.FavoriteCVRepository;
import com.best.cvapp.company.cv.dto.CompanyCVSummaryResponse;
import com.best.cvapp.cv.profile.CVRepository;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CVSearchService {

    private final CVRepository cvRepository;
    private final UserRepository        userRepository;
    private final CompanyRepository     companyRepository;
    private final FavoriteCVRepository  favoriteCVRepository;

    public Page<CompanyCVSummaryResponse> searchCVs(CVSearchRequest request, Pageable pageable) {
        Company company = getAuthenticatedCompany();

        CVSpecification spec = new CVSpecification(request);

        return cvRepository.findAll(spec, pageable).map(cv -> {
            boolean isFavorite = favoriteCVRepository.existsById(
                    new FavoriteCVId(company.getId(), cv.getId())
            );
            return new CompanyCVSummaryResponse(
                    cv.getId(),
                    cv.getFirstName(),
                    cv.getLastName(),
                    cv.getSummary(),
                    isFavorite
            );
        });
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Company getAuthenticatedCompany() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return companyRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
    }
}