package com.best.cvapp.company.history;

import com.best.cvapp.company.history.dto.CVViewResponse;
import com.best.cvapp.company.profile.Company;
import com.best.cvapp.company.profile.CompanyProfileService;
import com.best.cvapp.cv.profile.CV;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles CV viewing history for authenticated companies.
 *
 * Flow:
 * 1. Find the existing history entry for a company and CV.
 * 2. Create the entry when the CV has not been viewed before.
 * 3. Update the most recent viewing time.
 * 4. Load the authenticated company's history in newest-first order.
 * 5. Return the viewed CV summaries and timestamps.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CVHistoryService {

    private final CompanyProfileService companyProfileService;
    private final CVViewRepository cvViewRepository;

    @Transactional
    public void recordView(Company company, CV cv) {
        CVView view = cvViewRepository.findByCompanyAndCv(company, cv)
                .orElseGet(() -> CVView.builder()
                        .company(company)
                        .cv(cv)
                        .build());

        view.setViewedAt(LocalDateTime.now());

        cvViewRepository.save(view);
    }

    public List<CVViewResponse> getViewHistory() {
        Company company = companyProfileService.getAuthenticatedCompany();

        return cvViewRepository.findByCompanyOrderByViewedAtDesc(company)
                .stream()
                .map(view -> new CVViewResponse(
                        view.getCv().getId(),
                        view.getCv().getFirstName(),
                        view.getCv().getLastName(),
                        view.getViewedAt()
                ))
                .toList();
    }
}
