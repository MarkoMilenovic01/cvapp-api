package com.best.cvapp.company;

import com.best.cvapp.company.favorite.CvSummaryResponse;
import com.best.cvapp.company.favorite.FavoriteCV;
import com.best.cvapp.company.favorite.FavoriteCVId;
import com.best.cvapp.company.favorite.FavoriteCVRepository;
import com.best.cvapp.company.history.CvView;
import com.best.cvapp.company.history.CvViewRepository;
import com.best.cvapp.company.history.CvViewResponse;
import com.best.cvapp.cv.CV;
import com.best.cvapp.cv.CVRepository;
import com.best.cvapp.cv.CVService;
import com.best.cvapp.cv.CVResponse;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CvViewRepository cvViewRepository;
    private final FavoriteCVRepository favoriteCVRepository;
    private final CVRepository cvRepository;
    private final CVService cvService;
    private final UserRepository userRepository;

    // --- Company Profile ---

    public CompanyResponse getMyProfile() {
        Company company = getAuthenticatedCompany();
        return mapToResponse(company);
    }

    public CompanyResponse updateMyProfile(CompanyRequest request) {
        Company company = getAuthenticatedCompany();

        company.setName(request.getName());
        company.setDescription(request.getDescription());
        company.setWebsite(request.getWebsite());
        company.setIndustry(request.getIndustry());

        return mapToResponse(companyRepository.save(company));
    }

    // --- CV Browsing ---

    public Page<CvSummaryResponse> getAllCVs(Pageable pageable) {
        Company company = getAuthenticatedCompany();

        return cvRepository.findAll(pageable).map(cv -> {
            boolean isFavorite = favoriteCVRepository.existsById(
                    new FavoriteCVId(company.getId(), cv.getId())
            );
            return new CvSummaryResponse(
                    cv.getId(),
                    cv.getFirstName(),
                    cv.getLastName(),
                    cv.getSummary(),
                    isFavorite
            );
        });
    }

    @Transactional
    public CVResponse getCVById(Long cvId) {
        Company company = getAuthenticatedCompany();

        CV cv = cvRepository.findById(cvId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CV not found"));

        CvView view = cvViewRepository.findByCompanyAndCv(company, cv)
                .orElseGet(() -> CvView.builder()
                        .company(company)
                        .cv(cv)
                        .build());

        view.setViewedAt(LocalDateTime.now());

        cvViewRepository.save(view);

        return cvService.mapToResponse(cv);
    }

    // --- Favorites ---

    @Transactional
    public void addFavorite(Long cvId) {
        Company company = getAuthenticatedCompany();

        CV cv = cvRepository.findById(cvId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CV not found"));

        FavoriteCVId id = new FavoriteCVId(company.getId(), cv.getId());

        if (favoriteCVRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CV already in favorites");
        }

        favoriteCVRepository.save(FavoriteCV.builder()
                .id(id)
                .company(company)
                .cv(cv)
                .build());
    }

    public void removeFavorite(Long cvId) {
        Company company = getAuthenticatedCompany();
        FavoriteCVId id = new FavoriteCVId(company.getId(), cvId);

        if (!favoriteCVRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CV not in favorites");
        }

        favoriteCVRepository.deleteById(id);
    }

    public List<CvSummaryResponse> getFavorites() {
        Company company = getAuthenticatedCompany();

        return favoriteCVRepository.findByCompany(company).stream()
                .map(fav -> new CvSummaryResponse(
                        fav.getCv().getId(),
                        fav.getCv().getFirstName(),
                        fav.getCv().getLastName(),
                        fav.getCv().getSummary(),
                        true
                )).toList();
    }

    // --- View History ---

    public List<CvViewResponse> getViewHistory() {
        Company company = getAuthenticatedCompany();

        return cvViewRepository.findByCompanyOrderByViewedAtDesc(company).stream()
                .map(view -> new CvViewResponse(
                        view.getCv().getId(),
                        view.getCv().getFirstName(),
                        view.getCv().getLastName(),
                        view.getViewedAt()
                )).toList();
    }

    // --- Helpers ---

    private Company getAuthenticatedCompany() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return companyRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
    }

    private CompanyResponse mapToResponse(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getDescription(),
                company.getWebsite(),
                company.getIndustry(),
                company.getCreatedAt()
        );
    }
}