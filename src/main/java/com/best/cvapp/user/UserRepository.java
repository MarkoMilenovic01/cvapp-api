package com.best.cvapp.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


/**
 * Database access layer for User.
 * JpaRepository gives us save(), findById(), findAll(), delete() for free.
 * We only need to define custom queries here.
 */

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}