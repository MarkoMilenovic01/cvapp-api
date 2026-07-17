package com.best.cvapp.company.favorite;

import com.best.cvapp.company.profile.Company;
import com.best.cvapp.cv.profile.CV;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface FavoriteCVRepository extends JpaRepository<FavoriteCV, FavoriteCVId> {
    List<FavoriteCV> findByCompany(Company company);
    boolean existsById(FavoriteCVId id);
    void deleteByCompany(Company company);
    List<FavoriteCV> findByCv(CV cv);

    @Query("""
            select favorite.cv.id
            from FavoriteCV favorite
            where favorite.company = :company
              and favorite.cv.id in :cvIds
            """)
    Set<Long> findFavoriteCvIds(
            @Param("company") Company company,
            @Param("cvIds") Collection<Long> cvIds
    );
}
