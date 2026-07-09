package com.best.cvapp.admin.user;

import com.best.cvapp.admin.user.dto.AdminUserResponse;
import com.best.cvapp.auth.session.RefreshTokenService;
import com.best.cvapp.company.favorite.FavoriteCVRepository;
import com.best.cvapp.company.history.CVViewRepository;
import com.best.cvapp.company.profile.CompanyRepository;
import com.best.cvapp.cv.profile.CVRepository;
import com.best.cvapp.job.application.JobApplicationRepository;
import com.best.cvapp.job.core.Job;
import com.best.cvapp.job.core.JobRepository;
import com.best.cvapp.user.Role;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final CompanyRepository companyRepository;
    private final CVRepository cvRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final FavoriteCVRepository favoriteCVRepository;
    private final CVViewRepository cvViewRepository;
    private final JobRepository jobRepository;

    public Page<AdminUserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::toResponse);
    }

    public AdminUserResponse getUserById(Long id) {
        return toResponse(findUser(id));
    }

    @Transactional
    public AdminUserResponse toggleEnabled(Long id) {
        User user = findUser(id);
        user.setEnabled(!user.isEnabled());

        if (!user.isEnabled()) {
            refreshTokenService.deleteByUser(user);
        }

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public AdminUserResponse changeRole(Long id, Role role) {
        User user = findUser(id);
        user.setRole(role);
        refreshTokenService.deleteByUser(user);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findUser(id);

        // ── If this user has a CV (USER role) ──────────────────────────────
        cvRepository.findByUser(user).ifPresent(cv -> {
            jobApplicationRepository.deleteAll(jobApplicationRepository.findByCv(cv));
            favoriteCVRepository.deleteAll(favoriteCVRepository.findByCv(cv));
            cvViewRepository.deleteAll(cvViewRepository.findByCv(cv));
            cvRepository.delete(cv); // cascades to Education/Experience/Skill (already configured)
        });

        // ── If this user has a Company profile (COMPANY role) ─────────────
        companyRepository.findByUser(user).ifPresent(company -> {
            List<Job> jobs = jobRepository.findByCompany(company);
            for (Job job : jobs) {
                jobApplicationRepository.deleteAll(jobApplicationRepository.findByJob(job));
            }
            jobRepository.deleteAll(jobs);
            favoriteCVRepository.deleteAll(favoriteCVRepository.findByCompany(company));
            cvViewRepository.deleteAll(cvViewRepository.findByCompany(company));
            companyRepository.delete(company);
        });

        refreshTokenService.deleteByUser(user);
        userRepository.delete(user);
    }
    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }

    private AdminUserResponse toResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled(),
                user.getProvider(),
                user.getCreatedAt()
        );
    }
}