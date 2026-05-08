package io.kemalthes.semesterwork3.service;

import io.kemalthes.core.dto.CheckEmailExistsResponse;
import io.kemalthes.semesterwork3.dto.AuthValidationResponse;
import io.kemalthes.semesterwork3.entity.Role;
import io.kemalthes.semesterwork3.entity.User;
import io.kemalthes.semesterwork3.entity.enums.RoleName;
import io.kemalthes.semesterwork3.exception.AuthenticationRequiredException;
import io.kemalthes.semesterwork3.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public CheckEmailExistsResponse checkEmailExists(String email) {
        boolean exists = userRepository.existsByEmailIgnoreCase(email.trim());
        return new CheckEmailExistsResponse(exists);
    }

    public AuthValidationResponse validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AuthenticationRequiredException();
        }
        String token = authHeader.substring("Bearer ".length()).trim();
        try {
            UUID userId = UUID.fromString(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(AuthenticationRequiredException::new);
            return new AuthValidationResponse(user.getId(), resolveRole(user));
        } catch (IllegalArgumentException exception) {
            throw new AuthenticationRequiredException();
        }
    }

    private String resolveRole(User user) {
        boolean isAdmin = user.getRoles() != null && user.getRoles().stream()
                .map(Role::getName)
                .anyMatch(roleName -> roleName == RoleName.ADMIN);
        return isAdmin ? "ROLE_ADMIN" : "ROLE_USER";
    }
}
