package io.kemalthes.semesterwork3.mapper;

import io.kemalthes.core.dto.RouteFullResponse;
import io.kemalthes.core.dto.RoutePreviewResponse;
import io.kemalthes.semesterwork3.entity.TourRoute;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface RouteMapper {

    @Mapping(target = "distance", source = "distance", qualifiedByName = "routeBigDecimalToDouble")
    @Mapping(target = "authorName", source = "author.username")
    RoutePreviewResponse toRoutePreviewResponse(TourRoute route);

    List<RoutePreviewResponse> toRoutePreviewResponseList(List<TourRoute> routes);

    @Mapping(target = "distance", source = "distance", qualifiedByName = "routeBigDecimalToDouble")
    @Mapping(target = "authorName", source = "author.username")
    @Mapping(target = "reviews", ignore = true)
    RouteFullResponse toRouteFullResponse(TourRoute route);

    @Named("routeBigDecimalToDouble")
    default Double routeBigDecimalToDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

}
