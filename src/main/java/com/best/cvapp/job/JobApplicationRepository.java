package com.best.cvapp.job;

import com.best.cvapp.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    boolean existsByJobAndUser(Job job, User user);

    List<JobApplication> findByUserOrderByAppliedAtDesc(User user);

    List<JobApplication> findByJobOrderByAppliedAtDesc(Job job);

    Optional<JobApplication> findByIdAndUser(Long id, User user);
}