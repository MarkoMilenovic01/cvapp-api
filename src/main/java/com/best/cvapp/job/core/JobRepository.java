package com.best.cvapp.job.core;

import com.best.cvapp.company.profile.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    Page<Job> findByCompany(Company company, Pageable pageable);

    Optional<Job> findByIdAndCompany(Long id, Company company);

    Page<Job> findByActiveTrue(Pageable pageable);

    Optional<Job> findByIdAndActiveTrue(Long id);

    long countByActiveTrue();

    List<Job> findByCompany(Company company);

    Page<Job> findByCompanyAndActiveTrue(Company company, Pageable pageable);
}