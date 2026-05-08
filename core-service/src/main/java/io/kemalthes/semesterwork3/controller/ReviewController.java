package io.kemalthes.semesterwork3.controller;

import io.kemalthes.core.dto.PaginatedReviewsResponse;
import io.kemalthes.core.dto.ReviewRequest;
import io.kemalthes.core.dto.ReviewResponse;
import io.kemalthes.semesterwork3.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/routes/{routeId}/reviews")
@RequiredArgsConstructor
@Validated
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public PaginatedReviewsResponse getReviewsByRoute(
            @PathVariable UUID routeId,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit
    ) {
        return reviewService.getReviewsByRoute(routeId, page, limit);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse createReview(
            @PathVariable UUID routeId,
            @Valid @RequestBody ReviewRequest request
    ) {
        return reviewService.createReview(routeId, request);
    }

    @DeleteMapping("/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable UUID routeId, @PathVariable UUID reviewId) {
        reviewService.deleteReview(routeId, reviewId);
    }
}
