package io.kemalthes.searchservice.controller;

import io.kemalthes.search.dto.SearchRouteResponse;
import io.kemalthes.searchservice.service.RouteSearchService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final RouteSearchService routeSearchService;

    @GetMapping
    public List<SearchRouteResponse> searchRoutes(
            @NotNull @Size(min = 2) @Valid @RequestParam(value = "q") String q,
            @Min(1) @Max(20) @Valid @RequestParam(value = "limit", required = false, defaultValue = "12") Integer limit) {
        return routeSearchService.searchRoutes(q, limit);
    }
}
