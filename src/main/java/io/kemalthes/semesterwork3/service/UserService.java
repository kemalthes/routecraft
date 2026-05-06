package io.kemalthes.semesterwork3.service;

import io.kemalthes.core.dto.UserResponse;
import io.kemalthes.semesterwork3.entity.User;
import io.kemalthes.semesterwork3.exception.AuthenticationRequiredException;
import io.kemalthes.semesterwork3.exception.UserNotFoundException;
import io.kemalthes.semesterwork3.mapper.UserMapper;
import io.kemalthes.semesterwork3.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        UUID userId = currentUserService.getCurrentUserId();
        if (userId == null) {
            throw new AuthenticationRequiredException();
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return userMapper.toUserResponse(user);
    }
}
