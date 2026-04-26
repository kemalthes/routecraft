package io.kemalthes.semesterwork3.repository;

import io.kemalthes.semesterwork3.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
}
