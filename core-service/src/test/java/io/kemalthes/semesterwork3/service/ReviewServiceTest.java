package io.kemalthes.semesterwork3.service;

import io.kemalthes.core.dto.ReviewRequest;
import io.kemalthes.core.dto.ReviewResponse;
import io.kemalthes.semesterwork3.entity.Review;
import io.kemalthes.semesterwork3.entity.TourRoute;
import io.kemalthes.semesterwork3.entity.User;
import io.kemalthes.semesterwork3.entity.enums.RouteStatus;
import io.kemalthes.semesterwork3.exception.ReviewAccessDeniedException;
import io.kemalthes.semesterwork3.mapper.ReviewMapper;
import io.kemalthes.semesterwork3.repository.ReviewRepository;
import io.kemalthes.semesterwork3.repository.TourRouteRepository;
import io.kemalthes.semesterwork3.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private TourRouteRepository tourRouteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void getReviewsByRouteReturnsMappedPage() {
        UUID routeId = UUID.randomUUID();
        TourRoute route = route(routeId);
        Review review = new Review();
        ReviewResponse mapped = new ReviewResponse().id(UUID.randomUUID()).comment("Nice");

        when(tourRouteRepository.findByIdAndStatus(routeId, RouteStatus.PUBLISHED)).thenReturn(Optional.of(route));
        when(reviewRepository.findAllByRouteId(eq(routeId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(review)));
        when(reviewMapper.toReviewResponse(review)).thenReturn(mapped);

        var response = reviewService.getReviewsByRoute(routeId, 1, 5);

        assertEquals(List.of(mapped), response.getItems());
        assertEquals(1, response.getMeta().getCurrentPage());
        assertEquals(5, response.getMeta().getItemsPerPage());
    }

    @Test
    void createReviewTrimsCommentAndSavesAuthorAndRoute() {
        UUID routeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TourRoute route = route(routeId);
        User author = user(userId, "reviewer");
        ReviewResponse mapped = new ReviewResponse().comment("Great route");

        when(tourRouteRepository.findByIdAndStatus(routeId, RouteStatus.PUBLISHED)).thenReturn(Optional.of(route));
        when(currentUserService.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(author));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reviewMapper.toReviewResponse(any(Review.class))).thenReturn(mapped);

        var response = reviewService.createReview(routeId, new ReviewRequest(5, "  Great route  "));

        assertEquals(mapped, response);
        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        Review savedReview = reviewCaptor.getValue();
        assertEquals(5, savedReview.getRating());
        assertEquals("Great route", savedReview.getComment());
        assertEquals(author, savedReview.getUser());
        assertEquals(route, savedReview.getRoute());
    }

    @Test
    void createReviewRequiresAuthenticatedUser() {
        UUID routeId = UUID.randomUUID();
        when(tourRouteRepository.findByIdAndStatus(routeId, RouteStatus.PUBLISHED)).thenReturn(Optional.of(route(routeId)));
        when(currentUserService.getCurrentUserId()).thenReturn(null);

        assertThrows(ReviewAccessDeniedException.class,
                () -> reviewService.createReview(routeId, new ReviewRequest(4, "Good")));

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void deleteReviewAllowsOwner() {
        UUID routeId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Review review = review(reviewId, user(userId, "owner"));

        when(tourRouteRepository.findByIdAndStatus(routeId, RouteStatus.PUBLISHED)).thenReturn(Optional.of(route(routeId)));
        when(reviewRepository.findByIdAndRouteId(reviewId, routeId)).thenReturn(Optional.of(review));
        when(currentUserService.hasAdminRole()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(userId);

        reviewService.deleteReview(routeId, reviewId);

        verify(reviewRepository).delete(review);
    }

    @Test
    void deleteReviewRejectsNonOwner() {
        UUID routeId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        Review review = review(reviewId, user(UUID.randomUUID(), "owner"));

        when(tourRouteRepository.findByIdAndStatus(routeId, RouteStatus.PUBLISHED)).thenReturn(Optional.of(route(routeId)));
        when(reviewRepository.findByIdAndRouteId(reviewId, routeId)).thenReturn(Optional.of(review));
        when(currentUserService.hasAdminRole()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(UUID.randomUUID());

        assertThrows(ReviewAccessDeniedException.class, () -> reviewService.deleteReview(routeId, reviewId));

        verify(reviewRepository, never()).delete(any());
    }

    private static TourRoute route(UUID id) {
        return TourRoute.builder()
                .id(id)
                .status(RouteStatus.PUBLISHED)
                .author(user(UUID.randomUUID(), "author"))
                .build();
    }

    private static Review review(UUID id, User user) {
        Review review = new Review();
        review.setId(id);
        review.setUser(user);
        return review;
    }

    private static User user(UUID id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        return user;
    }
}
