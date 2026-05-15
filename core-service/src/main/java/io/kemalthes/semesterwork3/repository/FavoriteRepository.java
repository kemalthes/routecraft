package io.kemalthes.semesterwork3.repository;

import io.kemalthes.semesterwork3.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    boolean existsByUserIdAndRouteId(UUID userId, UUID routeId);

    @Query("""
            SELECT route.id
            FROM TourRoute route
            WHERE route.id IN :routeIds
              AND EXISTS (
                  SELECT favorite.id
                  FROM Favorite favorite
                  WHERE favorite.user.id = :userId
                    AND favorite.route = route
              )
            """)
    List<UUID> findLikedRouteIds(UUID userId, Collection<UUID> routeIds);

    @Query("SELECT f FROM Favorite f JOIN FETCH f.route r WHERE f.user.id = :userId")
    Page<Favorite> findAllByUserId(UUID userId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Favorite f WHERE f.user.id = :userId AND f.route.id = :routeId")
    void deleteByUserIdAndRouteId(UUID userId, UUID routeId);
}
