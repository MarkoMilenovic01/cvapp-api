package com.best.cvapp.job.core;

import com.best.cvapp.company.profile.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    Page<Job> findByCompany(Company company, Pageable pageable);

    Optional<Job> findByIdAndCompany(Long id, Company company);

    @Query("""
            select job from Job job
            where job.active = true
              and (job.deadline is null or job.deadline >= :today)
            """)
    Page<Job> findVisibleJobs(@Param("today") LocalDate today, Pageable pageable);

    @Query("""
            select job from Job job
            where job.id = :id
              and job.active = true
              and (job.deadline is null or job.deadline >= :today)
            """)
    Optional<Job> findVisibleJobById(
            @Param("id") Long id,
            @Param("today") LocalDate today
    );

    Optional<Job> findByIdAndActiveTrue(Long id);

    long countByActiveTrue();

    @Query("""
            select count(job) from Job job
            where job.active = true
              and (job.deadline is null or job.deadline >= :today)
            """)
    long countVisibleJobs(@Param("today") LocalDate today);

    List<Job> findByCompany(Company company);

    @Query("""
            select job from Job job
            where job.company = :company
              and job.active = true
              and (job.deadline is null or job.deadline >= :today)
            """)
    Page<Job> findVisibleJobsByCompany(
            @Param("company") Company company,
            @Param("today") LocalDate today,
            Pageable pageable
    );

}
