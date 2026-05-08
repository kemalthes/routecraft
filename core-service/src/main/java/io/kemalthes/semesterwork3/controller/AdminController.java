package io.kemalthes.semesterwork3.controller;

import io.kemalthes.core.dto.DeleteRouteRequest;
import io.kemalthes.core.dto.PaginatedRoutesResponse;
import io.kemalthes.core.dto.PaginatedUserResponse;
import io.kemalthes.core.dto.UpdateRouteRequest;
import io.kemalthes.semesterwork3.entity.enums.RouteStatus;
import io.kemalthes.semesterwork3.service.RouteService;
import io.kemalthes.semesterwork3.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final RouteService routeService;

    @GetMapping("/users")
    public PaginatedUserResponse getAllUsers(
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit
    ) {
        return userService.getAllUsers(page, limit);
    }

    @GetMapping("/routes")
    public PaginatedRoutesResponse getRoutes(
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit,
            @RequestParam(required = false) RouteStatus status
    ) {
        return routeService.getAdminRoutes(page, limit, status);
    }

    @PutMapping("/routes")
    public UUID approveRoute(@Valid @RequestBody UpdateRouteRequest updateRouteRequest) {
        return routeService.confirmRoute(updateRouteRequest);
        // TODO Разделение на микросервисы
        // TODO api gateway с X-header
        // TODO фильтр по хэдерам
        // TODO фронт с этой логикой

        // TODO кэширование
        // TODO spring ai
        // TODO nginx и деплой
    }

    @DeleteMapping("/routes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoute(@Valid @RequestBody DeleteRouteRequest request) {
        routeService.deleteRouteAdmin(request);
    }
}
