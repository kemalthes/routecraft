package io.kemalthes.semesterwork3.mapper;

import io.kemalthes.core.dto.LocationDto;
import io.kemalthes.semesterwork3.entity.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LocationMapper {

    @Mapping(target = "latitude", source = "latitude", qualifiedByName = "bigDecimalToDouble")
    @Mapping(target = "longitude", source = "longitude", qualifiedByName = "bigDecimalToDouble")
    LocationDto toLocationDto(Location location);

    List<LocationDto> toLocationDtoList(List<Location> locations);

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "latitude", source = "latitude", qualifiedByName = "doubleToBigDecimal")
    @Mapping(target = "longitude", source = "longitude", qualifiedByName = "doubleToBigDecimal")
    Location toLocation(LocationDto dto);

    @Named("bigDecimalToDouble")
    default Double bigDecimalToDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    @Named("doubleToBigDecimal")
    default BigDecimal doubleToBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

}
