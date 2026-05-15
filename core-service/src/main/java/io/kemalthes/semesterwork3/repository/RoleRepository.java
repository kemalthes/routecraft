package io.kemalthes.semesterwork3.repository;

import io.kemalthes.semesterwork3.entity.Role;
import io.kemalthes.semesterwork3.entity.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
