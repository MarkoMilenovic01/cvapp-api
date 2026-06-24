package com.best.cvapp.admin.user;

import com.best.cvapp.auth.session.RefreshTokenService;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

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