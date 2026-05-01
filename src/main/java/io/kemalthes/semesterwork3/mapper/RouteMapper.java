package io.kemalthes.semesterwork3.mapper;

import io.kemalthes.core.dto.RouteFullResponse;
import io.kemalthes.core.dto.RoutePreviewResponse;
import io.kemalthes.semesterwork3.entity.TourRoute;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface RouteMapper {

    @Mapping(target = "imageUrl", source = "presignedUrl")
    @Mapping(target = "distance", source = "route.distance", qualifiedByName = "routeBigDecimalToDouble")
    @Mapping(target = "authorName", source = "route.author.username")
    RoutePreviewResponse toRoutePreviewResponse(TourRoute route, String presignedUrl);

    @Mapping(target = "imageUrl", source = "presignedUrl")
    @Mapping(target = "distance", source = "route.distance", qualifiedByName = "routeBigDecimalToDouble")
    @Mapping(target = "authorName", source = "route.author.username")
    RouteFullResponse toRouteFullResponse(TourRoute route, String presignedUrl);

    @Named("routeBigDecimalToDouble")
    default Double routeBigDecimalToDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }
}
