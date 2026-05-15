package io.kemalthes.semesterwork3.controller;

import io.kemalthes.core.dto.AuthCodeRequest;
import io.kemalthes.core.dto.AuthResponse;
import io.kemalthes.core.dto.AuthValidationResponse;
import io.kemalthes.core.dto.ChangePasswordRequest;
import io.kemalthes.core.dto.CheckEmailExistsResponse;
import io.kemalthes.core.dto.LoginRequest;
import io.kemalthes.core.dto.RefreshTokenRequest;
import io.kemalthes.core.dto.RegisterRequest;
import io.kemalthes.core.dto.ResetPasswordRequest;
import io.kemalthes.semesterwork3.service.AuthService;
import io.kemalthes.semesterwork3.service.ClientIpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Validated
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ClientIpService clientIpService;

    @PostMapping("/registration-code")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendRegistrationCode(@Valid @RequestBody AuthCodeRequest request) {
        authService.sendRegistrationCode(request);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        return authService.register(request, clientIpService.resolve(httpRequest), httpRequest.getHeader(HttpHeaders.USER_AGENT));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return authService.login(request, clientIpService.resolve(httpRequest), httpRequest.getHeader(HttpHeaders.USER_AGENT));
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        return authService.refresh(request, clientIpService.resolve(httpRequest), httpRequest.getHeader(HttpHeaders.USER_AGENT));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            HttpServletRequest httpRequest
    ) {
        authService.logout(authHeader, clientIpService.resolve(httpRequest));
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            HttpServletRequest httpRequest
    ) {
        authService.changePassword(request, authHeader, clientIpService.resolve(httpRequest));
    }

    @PostMapping("/password-reset-code")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendPasswordResetCode(@Valid @RequestBody AuthCodeRequest request) {
        authService.sendPasswordResetCode(request);
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
    }

    @GetMapping("/check-email-exists")
    public CheckEmailExistsResponse checkEmailExists(
            @RequestParam @NotBlank @Email String email
    ) {
        return authService.checkEmailExists(email);
    }

    @GetMapping("/validate")
    public AuthValidationResponse validateToken(HttpServletRequest request) {
        return authService.validateToken(request.getHeader(HttpHeaders.AUTHORIZATION), clientIpService.resolve(request));
    }
}
