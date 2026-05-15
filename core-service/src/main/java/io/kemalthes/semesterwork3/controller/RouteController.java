package io.kemalthes.semesterwork3.controller;

import io.kemalthes.core.dto.CreateRouteRequest;
import io.kemalthes.core.dto.CreateRouteResponse;
import io.kemalthes.core.dto.PaginatedRoutesResponse;
import io.kemalthes.core.dto.RouteFullResponse;
import io.kemalthes.core.dto.UpdateRouteRequest;
import io.kemalthes.semesterwork3.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;

@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
@Validated
public class RouteController {

    private final RouteService routeService;

    @GetMapping
    public PaginatedRoutesResponse getRoutes(
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit,
            @RequestParam(required = false) String search
    ) {
        return routeService.getRoutes(page, limit, search);
    }

    @GetMapping("/my")
    public PaginatedRoutesResponse getMyRoutes(
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit
    ) {
        return routeService.getMyRoutes(page, limit);
    }

    @GetMapping("/my/{id}")
    public RouteFullResponse getMyRouteById(@PathVariable UUID id) {
        return routeService.getOwnUnpublishedRouteById(id);
    }

    @GetMapping("/{id}")
    public RouteFullResponse getRouteById(@PathVariable UUID id) {
        return routeService.getRouteById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateRouteResponse createRoute(@Valid @RequestBody CreateRouteRequest request) {
        return routeService.createRoute(request);
    }

    @PutMapping
    public UUID updateRoute(@Valid @RequestBody UpdateRouteRequest request) {
        return routeService.updateRoute(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoute(
            @PathVariable UUID id,
            @RequestParam(required = false) Long version
    ) {
        routeService.deleteOwnUnpublishedRoute(id, version);
    }
}
