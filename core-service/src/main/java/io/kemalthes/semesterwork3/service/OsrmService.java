package io.kemalthes.semesterwork3.service;

import io.kemalthes.core.dto.LocationDto;
import io.kemalthes.semesterwork3.config.CacheNames;
import io.kemalthes.semesterwork3.dto.OsrmResponse;
import io.kemalthes.semesterwork3.dto.OsrmRoute;
import io.kemalthes.semesterwork3.dto.OsrmRouteMetrics;
import io.kemalthes.semesterwork3.exception.BadRequestException;
import io.kemalthes.semesterwork3.exception.OsrmServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OsrmService {

    private final RestTemplate restTemplate;

    @Value("${osrm.base-url:https://router.project-osrm.org}")
    private String osrmBaseUrl;

    @Cacheable(cacheNames = CacheNames.OSRM_ROUTE_METRICS, key = "@routeCacheKeyService.locations(#locations)")
    public OsrmRouteMetrics calculateRoute(List<LocationDto> locations) {
        if (locations == null || locations.size() < 2) {
            throw new BadRequestException("Route should contain at least 2 locations");
        }
        List<LocationDto> sorted = locations.stream()
                .sorted(Comparator.comparing(LocationDto::getOrderIndex))
                .toList();
        String coordinates = sorted.stream()
                .map(location -> "%s,%s".formatted(location.getLongitude(), location.getLatitude()))
                .collect(Collectors.joining(";"));
        String requestUrl = "%s/route/v1/driving/%s?overview=full".formatted(osrmBaseUrl, coordinates);
        ResponseEntity<OsrmResponse> response;
        try {
            response = restTemplate.getForEntity(requestUrl, OsrmResponse.class);
        } catch (HttpStatusCodeException e) {
            log.warn("OSRM request failed with status {} for URL {}. Response body: {}",
                    e.getStatusCode().value(),
                    requestUrl,
                    e.getResponseBodyAsString());
            throw new OsrmServiceException("OSRM API call failed");
        } catch (RestClientException e) {
            log.warn("OSRM request failed for URL {}", requestUrl, e);
            throw new OsrmServiceException("OSRM API call failed");
        }
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new OsrmServiceException("OSRM API returned status code " + response.getStatusCode().value());
        }
        OsrmRoute route = getOsrmRoute(response);
        double distanceKm = route.distance() / 1000.0;
        int durationMinutes = (int) Math.round(route.duration() / 60.0);
        return new OsrmRouteMetrics(distanceKm, durationMinutes, route.geometry());
    }

    private OsrmRoute getOsrmRoute(ResponseEntity<OsrmResponse> response) {
        OsrmResponse osrmResponse = response.getBody();
        if (osrmResponse == null
                || !"Ok".equalsIgnoreCase(osrmResponse.code())
                || osrmResponse.routes() == null
                || osrmResponse.routes().isEmpty()
                || osrmResponse.routes().getFirst().distance() == null
                || osrmResponse.routes().getFirst().duration() == null
                || osrmResponse.routes().getFirst().geometry() == null) {
            throw new OsrmServiceException("OSRM API returned empty route data");
        }
        return osrmResponse.routes().getFirst();
    }
}
