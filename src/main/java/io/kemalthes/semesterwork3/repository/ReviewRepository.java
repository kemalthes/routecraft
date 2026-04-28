package io.kemalthes.semesterwork3.repository;

import io.kemalthes.semesterwork3.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
}
