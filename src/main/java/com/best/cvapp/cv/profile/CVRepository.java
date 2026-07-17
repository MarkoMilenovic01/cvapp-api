package com.best.cvapp.cv.profile;

import com.best.cvapp.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CVRepository
        extends JpaRepository<CV, Long>, JpaSpecificationExecutor<CV> {

    Optional<CV> findByUser(User user);

    boolean existsByUser(User user);
}