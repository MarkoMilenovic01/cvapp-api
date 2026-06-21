package com.best.cvapp.company.favorite;

import com.best.cvapp.company.cv.dto.CompanyCVSummaryResponse;
import com.best.cvapp.company.profile.Company;
import com.best.cvapp.company.profile.CompanyProfileService;
import com.best.cvapp.cv.CV;
import com.best.cvapp.cv.CVRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteCVService {

    private final CompanyProfileService companyProfileService;
    private final FavoriteCVRepository favoriteCVRepository;
    private final CVRepository cvRepository;

    @Transactional
    public void addFavorite(Long cvId) {
        Company company = companyProfileService.getAuthenticatedCompany();

        CV cv = cvRepository.findById(cvId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "CV not found"
                ));

        FavoriteCVId id = new FavoriteCVId(company.getId(), cv.getId());

        if (favoriteCVRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "CV already in favorites"
            );
        }

        FavoriteCV favorite = FavoriteCV.builder()
                .id(id)
                .company(company)
                .cv(cv)
                .build();

        favoriteCVRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(Long cvId) {
        Company company = companyProfileService.getAuthenticatedCompany();

        FavoriteCVId id = new FavoriteCVId(company.getId(), cvId);

        if (!favoriteCVRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "CV not in favorites"
            );
        }

        favoriteCVRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<CompanyCVSummaryResponse> getFavorites() {
        Company company = companyProfileService.getAuthenticatedCompany();

        return favoriteCVRepository.findByCompany(company)
                .stream()
                .map(favorite -> new CompanyCVSummaryResponse(
                        favorite.getCv().getId(),
                        favorite.getCv().getFirstName(),
                        favorite.getCv().getLastName(),
                        favorite.getCv().getSummary(),
                        true
                ))
                .toList();
    }
}