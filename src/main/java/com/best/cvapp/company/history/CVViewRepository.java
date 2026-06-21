package com.best.cvapp.company.history;

import com.best.cvapp.company.Company;
import com.best.cvapp.cv.CV;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CvViewRepository extends JpaRepository<CvView, Long> {

    List<CvView> findByCompanyOrderByViewedAtDesc(Company company);

    Optional<CvView> findByCompanyAndCv(Company company, CV cv);
}