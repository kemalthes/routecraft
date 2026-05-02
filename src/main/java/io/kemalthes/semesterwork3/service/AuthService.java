package io.kemalthes.semesterwork3.service;

import io.kemalthes.core.dto.CheckEmailExistsResponse;
import io.kemalthes.semesterwork3.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public CheckEmailExistsResponse checkEmailExists(String email) {
        boolean exists = userRepository.existsByEmailIgnoreCase(email.trim());
        return new CheckEmailExistsResponse(exists);
    }
}
