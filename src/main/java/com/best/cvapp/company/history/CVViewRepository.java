package com.best.cvapp.company.history;

import com.best.cvapp.company.profile.Company;
import com.best.cvapp.cv.profile.CV;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CVViewRepository extends JpaRepository<CVView, Long> {

    List<CVView> findByCompanyOrderByViewedAtDesc(Company company);

    Optional<CVView> findByCompanyAndCv(Company company, CV cv);
}