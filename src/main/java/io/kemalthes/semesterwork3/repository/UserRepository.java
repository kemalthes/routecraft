package io.kemalthes.semesterwork3.repository;

import io.kemalthes.semesterwork3.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}

