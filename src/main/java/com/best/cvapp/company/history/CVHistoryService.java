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