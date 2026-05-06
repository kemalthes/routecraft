package io.kemalthes.semesterwork3.mapper;

import io.kemalthes.core.dto.ReviewResponse;
import io.kemalthes.semesterwork3.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ReviewMapper {

    @Mapping(target = "authorName", source = "user.username")
    ReviewResponse toReviewResponse(Review review);
}
