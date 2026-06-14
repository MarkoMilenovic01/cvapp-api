package com.best.cvapp.cv;

import com.best.cvapp.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CVRepository extends JpaRepository<CV, Long> {
    Optional<CV> findByUser(User user);
    boolean existsByUser(User user);
}