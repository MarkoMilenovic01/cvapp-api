package com.best.cvapp.company.favorite;

import com.best.cvapp.company.cv.dto.CompanyCVSummaryResponse;
import com.best.cvapp.company.favorite.exception.CVAlreadyFavoritedException;
import com.best.cvapp.company.favorite.exception.CVNotFavoritedException;
import com.best.cvapp.company.favorite.exception.FavoriteCVNotFoundException;
import com.best.cvapp.company.profile.Company;
import com.best.cvapp.company.profile.CompanyProfileService;
import com.best.cvapp.cv.profile.CV;
import com.best.cvapp.cv.profile.CVRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handles favorite CVs for the authenticated company.
 *
 * Flow:
 * 1. Load the company belonging to the authenticated user.
 * 2. Validate that the requested CV exists.
 * 3. Add the CV to favorites unless it is already present.
 * 4. Remove an existing favorite when requested.
 * 5. Return the company's favorite CV summaries.
 */
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
                .orElseThrow(FavoriteCVNotFoundException::new);

        FavoriteCVId id = new FavoriteCVId(company.getId(), cv.getId());

        if (favoriteCVRepository.existsById(id)) {
            throw new CVAlreadyFavoritedException();
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
            throw new CVNotFavoritedException();
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
