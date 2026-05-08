package io.kemalthes.semesterwork3.mapper;

import io.kemalthes.core.dto.RouteFullResponse;
import io.kemalthes.core.dto.RoutePreviewResponse;
import io.kemalthes.core.dto.RouteStatus;
import io.kemalthes.semesterwork3.entity.TourRoute;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = ReviewMapper.class
)
public interface RouteMapper {

    @Mapping(target = "imageUrl", source = "presignedUrl")
    @Mapping(target = "distance", source = "route.distance", qualifiedByName = "routeBigDecimalToDouble")
    @Mapping(target = "authorName", source = "route.author.username")
    @Mapping(target = "status", source = "route", qualifiedByName = "routeStatusToDto")
    @Mapping(target = "isLiked", source = "isLiked")
    RoutePreviewResponse toRoutePreviewResponse(TourRoute route, String presignedUrl, boolean isLiked);

    @Mapping(target = "imageUrl", source = "presignedUrl")
    @Mapping(target = "distance", source = "route.distance", qualifiedByName = "routeBigDecimalToDouble")
    @Mapping(target = "authorName", source = "route.author.username")
    @Mapping(target = "reviews", source = "route.reviews")
    @Mapping(target = "status", source = "route", qualifiedByName = "routeStatusToDto")
    @Mapping(target = "isLiked", source = "isLiked")
    RouteFullResponse toRouteFullResponse(TourRoute route, String presignedUrl, boolean isLiked);

    @Named("routeBigDecimalToDouble")
    default Double routeBigDecimalToDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    @Named("routeStatusToDto")
    default RouteStatus routeStatusToDto(TourRoute route) {
        return route == null || route.getStatus() == null ? null : RouteStatus.fromValue(route.getStatus().name());
    }
}
