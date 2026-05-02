package io.kemalthes.semesterwork3.controller;

import io.kemalthes.core.dto.CheckEmailExistsResponse;
import io.kemalthes.semesterwork3.repository.UserRepository;
import io.kemalthes.semesterwork3.service.AuthService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Validated
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/check-email-exists")
    public CheckEmailExistsResponse checkEmailExists(
            @RequestParam @NotBlank @Email String email
    ) {
        return authService.checkEmailExists(email);
    }
}
