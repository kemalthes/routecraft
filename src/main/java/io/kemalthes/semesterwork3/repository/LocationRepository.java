package io.kemalthes.semesterwork3.repository;

import io.kemalthes.semesterwork3.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {
}

