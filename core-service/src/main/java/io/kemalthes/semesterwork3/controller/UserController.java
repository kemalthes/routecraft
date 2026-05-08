package io.kemalthes.semesterwork3.controller;

import io.kemalthes.core.dto.UserResponse;
import io.kemalthes.semesterwork3.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse getCurrentUser() {
        return userService.getCurrentUser();
    }
}
