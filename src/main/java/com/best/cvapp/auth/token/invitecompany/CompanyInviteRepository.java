package com.best.cvapp.auth.token.invitecompany;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyInviteRepository extends JpaRepository<CompanyInvite, Long> {
    Optional<CompanyInvite> findByToken(String token);
    boolean existsByEmail(String email);
}