package io.kemalthes.semesterwork3.service;

import io.kemalthes.core.dto.PaginatedReviewsResponse;
import io.kemalthes.core.dto.PaginationMeta;
import io.kemalthes.core.dto.ReviewRequest;
import io.kemalthes.core.dto.ReviewResponse;
import io.kemalthes.semesterwork3.entity.Review;
import io.kemalthes.semesterwork3.entity.TourRoute;
import io.kemalthes.semesterwork3.entity.User;
import io.kemalthes.semesterwork3.entity.enums.RouteStatus;
import io.kemalthes.semesterwork3.exception.ReviewAccessDeniedException;
import io.kemalthes.semesterwork3.exception.ReviewNotFoundException;
import io.kemalthes.semesterwork3.exception.RouteNotFoundException;
import io.kemalthes.semesterwork3.exception.UserNotFoundException;
import io.kemalthes.semesterwork3.mapper.ReviewMapper;
import io.kemalthes.semesterwork3.repository.ReviewRepository;
import io.kemalthes.semesterwork3.repository.TourRouteRepository;
import io.kemalthes.semesterwork3.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final TourRouteRepository tourRouteRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final ReviewMapper reviewMapper;

    @Transactional(readOnly = true)
    public PaginatedReviewsResponse getReviewsByRoute(UUID routeId, Integer page, Integer limit) {
        TourRoute route = findPublishedRoute(routeId);
        int currentPage = page == null ? 1 : page;
        int itemsPerPage = limit == null ? 10 : limit;
        Pageable pageable = PageRequest.of(
                currentPage - 1,
                itemsPerPage,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
        );
        Page<Review> pageResult = reviewRepository.findAllByRouteId(route.getId(), pageable);
        int totalItems = (int) Math.min(Integer.MAX_VALUE, pageResult.getTotalElements());
        PaginationMeta meta = new PaginationMeta()
                .totalItems(totalItems)
                .totalPages(pageResult.getTotalPages())
                .currentPage(currentPage)
                .itemsPerPage(itemsPerPage);
        return new PaginatedReviewsResponse()
                .items(pageResult.getContent().stream().map(reviewMapper::toReviewResponse).toList())
                .meta(meta);
    }

    @Transactional
    public ReviewResponse createReview(UUID routeId, ReviewRequest request) {
        TourRoute route = findPublishedRoute(routeId);
        UUID currentUserId = currentUserService.getCurrentUserId();
        if (currentUserId == null) {
            throw new ReviewAccessDeniedException();
        }
        User author = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(currentUserId));
        Review review = new Review();
        review.setId(UUID.randomUUID());
        review.setRating(request.getRating());
        review.setComment(request.getComment().trim());
        review.setUser(author);
        review.setRoute(route);
        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toReviewResponse(savedReview);
    }

    @Transactional
    public void deleteReview(UUID routeId, UUID reviewId) {
        findPublishedRoute(routeId);
        if (reviewId == null) {
            throw new ReviewNotFoundException(null);
        }
        Review review = reviewRepository.findByIdAndRouteId(reviewId, routeId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));
        if (!currentUserService.hasAdminRole()) {
            UUID currentUserId = currentUserService.getCurrentUserId();
            boolean isOwner = currentUserId != null
                    && review.getUser() != null
                    && currentUserId.equals(review.getUser().getId());
            if (!isOwner) {
                throw new ReviewAccessDeniedException();
            }
        }
        reviewRepository.delete(review);
    }

    private TourRoute findPublishedRoute(UUID routeId) {
        if (routeId == null) {
            throw new RouteNotFoundException(null);
        }
        return tourRouteRepository.findByIdAndStatus(routeId, RouteStatus.PUBLISHED)
                .orElseThrow(() -> new RouteNotFoundException(routeId));
    }
}
