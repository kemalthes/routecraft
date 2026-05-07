package io.kemalthes.semesterwork3.controller;

import io.kemalthes.core.dto.PaginatedRoutesResponse;
import io.kemalthes.semesterwork3.service.FavoriteService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoritesController {

    private final FavoriteService favoriteService;

    @GetMapping
    public PaginatedRoutesResponse getRoutes(
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit
    ) {
        return favoriteService.getFavorites(page, limit);
    }

    @PostMapping("/{routeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addRouteToFavorites(@PathVariable UUID routeId) {
        favoriteService.addRouteToFavorites(routeId);
    }

    @DeleteMapping("/{routeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeRouteFromFavorites(@PathVariable UUID routeId) {
        favoriteService.removeRouteFromFavorites(routeId);
    }

}
