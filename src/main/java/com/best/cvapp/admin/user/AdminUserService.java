package com.best.cvapp.admin.user;

import com.best.cvapp.admin.user.dto.AdminUserResponse;
import com.best.cvapp.admin.user.exception.AdminSelfModificationException;
import com.best.cvapp.admin.user.exception.AdminUserNotFoundException;
import com.best.cvapp.admin.user.exception.InvalidAdminRoleChangeException;
import com.best.cvapp.admin.user.exception.LastEnabledAdministratorException;
import com.best.cvapp.auth.session.RefreshTokenService;
import com.best.cvapp.company.favorite.FavoriteCVRepository;
import com.best.cvapp.company.history.CVViewRepository;
import com.best.cvapp.company.profile.CompanyRepository;
import com.best.cvapp.cv.profile.CVRepository;
import com.best.cvapp.job.application.JobApplicationRepository;
import com.best.cvapp.job.core.Job;
import com.best.cvapp.job.core.JobRepository;
import com.best.cvapp.shared.storage.CloudinaryService;
import com.best.cvapp.user.Role;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handles administrator management of user accounts and owned data.
 *
 * Flow:
 * 1. Load users with pagination or find one user by ID.
 * 2. Prevent unsafe self-modification and protect the final enabled administrator.
 * 3. Validate role changes and revoke refresh tokens after security-sensitive changes.
 * 4. Delete CV or company dependencies and their Cloudinary files when removing a user.
 * 5. Map the resulting account state to an admin response.
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final CompanyRepository companyRepository;
    private final CVRepository cvRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final FavoriteCVRepository favoriteCVRepository;
    private final CVViewRepository cvViewRepository;
    private final JobRepository jobRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(Long id) {
        return toResponse(findUser(id));
    }

    @Transactional
    public AdminUserResponse toggleEnabled(Long id, User currentAdmin) {
        User user = findUser(id);
        rejectSelfModification(user, currentAdmin, "disable");

        if (user.isEnabled()) {
            protectLastEnabledAdministrator(user);
        }
        user.setEnabled(!user.isEnabled());

        if (!user.isEnabled()) {
            refreshTokenService.deleteByUser(user);
        }

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public AdminUserResponse changeRole(Long id, Role role, User currentAdmin) {
        User user = findUser(id);
        if (role != Role.ADMIN) {
            rejectSelfModification(user, currentAdmin, "demote");
            protectLastEnabledAdministrator(user);
        }

        validateRoleChange(user, role);
        user.setRole(role);
        refreshTokenService.deleteByUser(user);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id, User currentAdmin) {
        User user = findUser(id);
        rejectSelfModification(user, currentAdmin, "delete");
        protectLastEnabledAdministrator(user);

        // ── If this user has a CV (USER role) ──────────────────────────────
        cvRepository.findByUser(user).ifPresent(cv -> {
            deleteCvFiles(cv.getProfilePhotoId(), cv.getPdfPublicId());
            jobApplicationRepository.deleteAll(jobApplicationRepository.findByCv(cv));
            favoriteCVRepository.deleteAll(favoriteCVRepository.findByCv(cv));
            cvViewRepository.deleteAll(cvViewRepository.findByCv(cv));
            cvRepository.delete(cv); // cascades to Education/Experience/Skill
        });

        // ── If this user has a Company profile (COMPANY role) ─────────────
        companyRepository.findByUser(user).ifPresent(company -> {
            deleteCompanyPhoto(company.getPhotoPublicId());
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

    private void deleteCvFiles(String profilePhotoId, String pdfPublicId) {
        if (profilePhotoId != null) {
            cloudinaryService.deleteImage(profilePhotoId);
        }
        if (pdfPublicId != null) {
            cloudinaryService.deletePdf(pdfPublicId);
        }
    }

    private void deleteCompanyPhoto(String photoPublicId) {
        if (photoPublicId != null) {
            cloudinaryService.deleteImage(photoPublicId);
        }
    }

    private void rejectSelfModification(User target, User currentAdmin, String action) {
        if (target.getId().equals(currentAdmin.getId())) {
            throw new AdminSelfModificationException(action);
        }
    }

    private void protectLastEnabledAdministrator(User user) {
        if (user.getRole() == Role.ADMIN
                && user.isEnabled()
                && userRepository.countByRoleAndEnabledTrue(Role.ADMIN) <= 1) {
            throw new LastEnabledAdministratorException();
        }
    }

    private void validateRoleChange(User user, Role requestedRole) {
        if (requestedRole == user.getRole()) {
            return;
        }

        boolean userToAdmin = user.getRole() == Role.USER && requestedRole == Role.ADMIN;
        boolean adminToUser = user.getRole() == Role.ADMIN && requestedRole == Role.USER;

        if (!userToAdmin && !adminToUser) {
            throw new InvalidAdminRoleChangeException("Only USER and ADMIN roles can be changed");
        }
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(AdminUserNotFoundException::new);
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
