package io.kemalthes.semesterwork3.service;

import io.kemalthes.core.dto.UserResponse;
import io.kemalthes.semesterwork3.entity.User;
import io.kemalthes.semesterwork3.exception.AuthenticationRequiredException;
import io.kemalthes.semesterwork3.exception.UserAccessDeniedException;
import io.kemalthes.semesterwork3.mapper.UserMapper;
import io.kemalthes.semesterwork3.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void getCurrentUserReturnsMappedAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        User user = user(userId, "alice");
        UserResponse mapped = new UserResponse().id(userId).username("alice").role("USER");

        when(currentUserService.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(mapped);

        assertEquals(mapped, userService.getCurrentUser());
    }

    @Test
    void getCurrentUserRequiresAuthentication() {
        when(currentUserService.getCurrentUserId()).thenReturn(null);

        assertThrows(AuthenticationRequiredException.class, () -> userService.getCurrentUser());

        verify(userRepository, never()).findById(any());
    }

    @Test
    void getAllUsersReturnsPageForAdmin() {
        User user = user(UUID.randomUUID(), "bob");
        UserResponse mapped = new UserResponse().id(user.getId()).username("bob").role("USER");

        when(currentUserService.hasAdminRole()).thenReturn(true);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(user)));
        when(userMapper.toUserResponseList(eq(List.of(user)))).thenReturn(List.of(mapped));

        var response = userService.getAllUsers(2, 20);

        assertEquals(List.of(mapped), response.getItems());
        assertEquals(2, response.getMeta().getCurrentPage());
        assertEquals(20, response.getMeta().getItemsPerPage());
    }

    @Test
    void getAllUsersRequiresAdminRole() {
        when(currentUserService.hasAdminRole()).thenReturn(false);

        assertThrows(UserAccessDeniedException.class, () -> userService.getAllUsers(1, 10));

        verify(userRepository, never()).findAll(any(Pageable.class));
    }

    private static User user(UUID id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        return user;
    }
}
