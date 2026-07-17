package com.best.cvapp.company.cv;

import com.best.cvapp.company.cv.dto.CVSearchRequest;
import com.best.cvapp.company.profile.Company;
import com.best.cvapp.company.profile.CompanyProfileService;
import com.best.cvapp.company.favorite.FavoriteCVRepository;
import com.best.cvapp.company.cv.dto.CompanyCVSummaryResponse;
import com.best.cvapp.cv.profile.CV;
import com.best.cvapp.cv.profile.CVRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Handles filtered CV searches for the authenticated company.
 *
 * Flow:
 * 1. Load the company belonging to the authenticated user.
 * 2. Build a CV specification from the submitted search filters.
 * 3. Search matching CVs using the requested pagination.
 * 4. Load the company's favorite identifiers for the result page.
 * 5. Return a page of CV summaries.
 */
@Service
@RequiredArgsConstructor
public class CVSearchService {

    private final CVRepository cvRepository;
    private final CompanyProfileService companyProfileService;
    private final FavoriteCVRepository  favoriteCVRepository;

    public Page<CompanyCVSummaryResponse> searchCVs(CVSearchRequest request, Pageable pageable) {
        Company company = companyProfileService.getAuthenticatedCompany();

        CVSpecification spec = new CVSpecification(request);

        Page<CV> cvs = cvRepository.findAll(spec, pageable);
        Set<Long> favoriteCvIds = findFavoriteCvIds(company, cvs);

        return cvs.map(cv -> new CompanyCVSummaryResponse(
                    cv.getId(),
                    cv.getFirstName(),
                    cv.getLastName(),
                    cv.getSummary(),
                    favoriteCvIds.contains(cv.getId())
            ));
    }

    private Set<Long> findFavoriteCvIds(Company company, Page<CV> cvs) {
        if (cvs.isEmpty()) {
            return Set.of();
        }

        return favoriteCVRepository.findFavoriteCvIds(
                company,
                cvs.stream().map(CV::getId).toList()
        );
    }
}
