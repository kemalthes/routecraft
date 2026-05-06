package io.kemalthes.semesterwork3.mapper;

import io.kemalthes.core.dto.UserResponse;
import io.kemalthes.semesterwork3.entity.Role;
import io.kemalthes.semesterwork3.entity.User;
import io.kemalthes.semesterwork3.entity.enums.RoleName;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

    @Mapping(target = "role", expression = "java(resolveRole(user))")
    UserResponse toUserResponse(User user);

    default String resolveRole(User user) {
        if (user == null || user.getRoles() == null) {
            return "USER";
        }
        return user.getRoles().stream()
                .map(Role::getName)
                .anyMatch(roleName -> roleName == RoleName.ADMIN)
                ? "ADMIN"
                : "USER";
    }
}
