package com.best.cvapp.company.cv;

import com.best.cvapp.company.cv.dto.CVSearchRequest;
import com.best.cvapp.company.cv.dto.CompanyCVSummaryResponse;
import com.best.cvapp.company.cv.exception.CompanyCVNotFoundException;
import com.best.cvapp.company.favorite.FavoriteCVRepository;
import com.best.cvapp.company.history.CVHistoryService;
import com.best.cvapp.company.profile.Company;
import com.best.cvapp.company.profile.CompanyProfileService;
import com.best.cvapp.cv.profile.CV;
import com.best.cvapp.cv.profile.CVRepository;
import com.best.cvapp.cv.profile.dto.CVResponse;
import com.best.cvapp.cv.profile.CVService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Handles CV access for authenticated companies.
 *
 * Flow:
 * 1. Load the company belonging to the authenticated user.
 * 2. List available CVs with their favorite status.
 * 3. Load a requested CV and record that the company viewed it.
 * 4. Delegate filtered searches to the CV search service.
 * 5. Return CV summaries or the complete CV details.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyCVService {

    private final CompanyProfileService companyProfileService;
    private final CVRepository cvRepository;
    private final CVService cvService;
    private final CVSearchService cvSearchService;
    private final FavoriteCVRepository favoriteCVRepository;
    private final CVHistoryService cvHistoryService;

    public Page<CompanyCVSummaryResponse> getAllCVs(Pageable pageable) {
        Company company = companyProfileService.getAuthenticatedCompany();

        Page<CV> cvs = cvRepository.findAll(pageable);
        Set<Long> favoriteCvIds = findFavoriteCvIds(company, cvs);

        return cvs.map(cv -> mapToSummary(cv, favoriteCvIds.contains(cv.getId())));
    }

    @Transactional
    public CVResponse getCVById(Long cvId) {
        Company company = companyProfileService.getAuthenticatedCompany();

        CV cv = cvRepository.findById(cvId)
                .orElseThrow(CompanyCVNotFoundException::new);

        cvHistoryService.recordView(company, cv);

        return cvService.mapToResponse(cv);
    }

    public Page<CompanyCVSummaryResponse> searchCVs(
            CVSearchRequest request,
            Pageable pageable
    ) {
        return cvSearchService.searchCVs(request, pageable);
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

    private CompanyCVSummaryResponse mapToSummary(CV cv, boolean favorite) {
        return new CompanyCVSummaryResponse(
                cv.getId(),
                cv.getFirstName(),
                cv.getLastName(),
                cv.getSummary(),
                favorite
        );
    }
}
