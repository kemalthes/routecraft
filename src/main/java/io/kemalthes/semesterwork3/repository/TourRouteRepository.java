package io.kemalthes.semesterwork3.repository;

import io.kemalthes.semesterwork3.entity.TourRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TourRouteRepository extends JpaRepository<TourRoute, Long>, JpaSpecificationExecutor<TourRoute> {
}

