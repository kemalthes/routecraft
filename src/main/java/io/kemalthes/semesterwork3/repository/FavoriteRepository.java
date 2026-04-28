package io.kemalthes.semesterwork3.repository;

import io.kemalthes.semesterwork3.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
}
