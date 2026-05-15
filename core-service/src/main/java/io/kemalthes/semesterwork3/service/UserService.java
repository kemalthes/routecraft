package io.kemalthes.semesterwork3.service;

import io.kemalthes.core.dto.PaginatedUserResponse;
import io.kemalthes.core.dto.PaginationMeta;
import io.kemalthes.core.dto.UserResponse;
import io.kemalthes.semesterwork3.entity.User;
import io.kemalthes.semesterwork3.exception.AuthenticationRequiredException;
import io.kemalthes.semesterwork3.exception.UserAccessDeniedException;
import io.kemalthes.semesterwork3.exception.UserNotFoundException;
import io.kemalthes.semesterwork3.mapper.UserMapper;
import io.kemalthes.semesterwork3.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public PaginatedUserResponse getAllUsers(Integer page, Integer limit) {
        if (!currentUserService.hasAdminRole()) {
            throw new UserAccessDeniedException();
        }
        int currentPage = page == null ? 1 : page;
        int itemsPerPage = limit == null ? 10 : limit;
        Pageable pageable = PageRequest.of(currentPage - 1, itemsPerPage, Sort.by(Sort.Direction.ASC, "id"));
        Page<User> pageResult = userRepository.findAll(pageable);
        int totalItems = (int) Math.min(Integer.MAX_VALUE, pageResult.getTotalElements());
        PaginationMeta meta = new PaginationMeta()
                .totalItems(totalItems)
                .totalPages(pageResult.getTotalPages())
                .currentPage(currentPage)
                .itemsPerPage(itemsPerPage);
        return new PaginatedUserResponse()
                .items(userMapper.toUserResponseList(pageResult.getContent()))
                .meta(meta);
    }
}
