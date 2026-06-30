package com.best.cvapp.company.favorite;

import com.best.cvapp.company.profile.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteCVRepository extends JpaRepository<FavoriteCV, FavoriteCVId> {
    List<FavoriteCV> findByCompany(Company company);
    boolean existsById(FavoriteCVId id);
    void deleteByCompany(Company company);
}