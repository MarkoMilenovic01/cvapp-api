package com.best.cvapp.company.favorite;

import com.best.cvapp.company.cv.dto.CompanyCVSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COMPANY')")
public class FavoriteCVController {

    private final FavoriteCVService favoriteCVService;

    @PostMapping("/cvs/{id}/favorite")
    public ResponseEntity<Void> addFavorite(@PathVariable Long id) {
        favoriteCVService.addFavorite(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cvs/{id}/favorite")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long id) {
        favoriteCVService.removeFavorite(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<CompanyCVSummaryResponse>> getFavorites() {
        return ResponseEntity.ok(favoriteCVService.getFavorites());
    }
}