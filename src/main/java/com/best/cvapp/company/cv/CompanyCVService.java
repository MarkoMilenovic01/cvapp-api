package com.best.cvapp.company.cv;

import com.best.cvapp.company.cv.dto.CVSearchRequest;
import com.best.cvapp.company.cv.dto.CompanyCVSummaryResponse;
import com.best.cvapp.company.favorite.FavoriteCVId;
import com.best.cvapp.company.favorite.FavoriteCVRepository;
import com.best.cvapp.company.history.CVHistoryService;
import com.best.cvapp.company.profile.Company;
import com.best.cvapp.company.profile.CompanyProfileService;
import com.best.cvapp.cv.profile.CV;
import com.best.cvapp.cv.profile.CVRepository;
import com.best.cvapp.cv.profile.CVResponse;
import com.best.cvapp.cv.profile.CVService;
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
public class CompanyCVService {

    private final CompanyProfileService companyProfileService;
    private final CVRepository cvRepository;
    private final CVService cvService;
    private final CVSearchService cvSearchService;
    private final FavoriteCVRepository favoriteCVRepository;
    private final CVHistoryService cvHistoryService;

    public Page<CompanyCVSummaryResponse> getAllCVs(Pageable pageable) {
        Company company = companyProfileService.getAuthenticatedCompany();

        return cvRepository.findAll(pageable)
                .map(cv -> mapToSummary(cv, company));
    }

    @Transactional
    public CVResponse getCVById(Long cvId) {
        Company company = companyProfileService.getAuthenticatedCompany();

        CV cv = cvRepository.findById(cvId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "CV not found"
                ));

        cvHistoryService.recordView(company, cv);

        return cvService.mapToResponse(cv);
    }

    public Page<CompanyCVSummaryResponse> searchCVs(
            CVSearchRequest request,
            Pageable pageable
    ) {
        return cvSearchService.searchCVs(request, pageable);
    }

    private CompanyCVSummaryResponse mapToSummary(CV cv, Company company) {
        boolean favorite = favoriteCVRepository.existsById(
                new FavoriteCVId(company.getId(), cv.getId())
        );

        return new CompanyCVSummaryResponse(
                cv.getId(),
                cv.getFirstName(),
                cv.getLastName(),
                cv.getSummary(),
                favorite
        );
    }
}