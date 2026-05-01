package io.kemalthes.semesterwork3.repository;

import io.kemalthes.semesterwork3.entity.TourRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TourRouteRepository extends JpaRepository<TourRoute, UUID>, JpaSpecificationExecutor<TourRoute> {

    @Query("""
        SELECT t FROM TourRoute t WHERE
        LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR
        LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%'))
        """)
    Page<TourRoute> searchByTitleOrDescription(@Param("query") String query, Pageable pageable);

    Optional<TourRoute> findByImageUrl(String imageUrl);
}

