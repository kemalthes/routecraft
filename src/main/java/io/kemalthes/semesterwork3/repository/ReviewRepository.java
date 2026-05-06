package io.kemalthes.semesterwork3.repository;

import io.kemalthes.semesterwork3.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    @EntityGraph(attributePaths = {"user"})
    Page<Review> findAllByRouteId(UUID routeId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "route"})
    Optional<Review> findByIdAndRouteId(UUID reviewId, UUID routeId);
}
