package io.kemalthes.semesterwork3.service;

import io.kemalthes.core.dto.CreateRouteRequest;
import io.kemalthes.core.dto.LocationDto;
import io.kemalthes.core.dto.PaginatedRoutesResponse;
import io.kemalthes.core.dto.PaginationMeta;
import io.kemalthes.core.dto.RouteFullResponse;
import io.kemalthes.core.dto.UpdateRouteRequest;
import io.kemalthes.semesterwork3.dto.OsrmRouteMetrics;
import io.kemalthes.semesterwork3.entity.TourRoute;
import io.kemalthes.semesterwork3.entity.User;
import io.kemalthes.semesterwork3.exception.BadRequestException;
import io.kemalthes.semesterwork3.exception.RouteAccessDeniedException;
import io.kemalthes.semesterwork3.exception.RouteNotFoundException;
import io.kemalthes.semesterwork3.exception.UserNotFoundException;
import io.kemalthes.semesterwork3.mapper.LocationMapper;
import io.kemalthes.semesterwork3.mapper.RouteMapper;
import io.kemalthes.semesterwork3.repository.TourRouteRepository;
import io.kemalthes.semesterwork3.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final TourRouteRepository tourRouteRepository;
    private final UserRepository userRepository;
    private final OsrmService osrmService;
    private final RouteMapper routeMapper;
    private final LocationMapper locationMapper;
    private final CurrentUserService currentUserService;

    @Transactional
    public UUID createRoute(CreateRouteRequest request) {
        validateLocations(request.getLocations());
        UUID authorId = currentUserService.getCurrentUserId();
        User author = resolveAuthor(authorId);
        OsrmRouteMetrics metrics = osrmService.calculateRoute(request.getLocations());
        TourRoute route = TourRoute.builder()
                .id(UUID.randomUUID())
                .title(request.getTitle())
                .description(request.getDescription() == null ? "" : request.getDescription())
                .imageUrl(request.getImageUrl())
                .distance(BigDecimal.valueOf(metrics.distanceKm()))
                .durationMinutes(metrics.durationMinutes())
                .geometry(metrics.geometry())
                .author(author)
                .build();
        request.getLocations().stream()
                .sorted(Comparator.comparing(LocationDto::getOrderIndex))
                .map(locationMapper::toLocation)
                .forEach(location -> {
                    location.setRoute(route);
                    route.getLocations().add(location);
                });
        TourRoute savedRoute = tourRouteRepository.save(route);
        return savedRoute.getId();
    }

    @Transactional(readOnly = true)
    public PaginatedRoutesResponse getRoutes(Integer page, Integer limit, String search) {
        int currentPage = page == null ? 1 : page;
        int itemsPerPage = limit == null ? 10 : limit;
        String query = search == null ? null : search.trim();
        Pageable pageable = PageRequest.of(currentPage - 1, itemsPerPage, Sort.by(Sort.Direction.ASC, "id"));
        Page<TourRoute> pageResult;
        if (query == null || query.isEmpty()) {
            pageResult = tourRouteRepository.findAll(pageable);
        } else {
            pageResult = tourRouteRepository.searchByTitleOrDescription(query, pageable);
        }
        int totalItems = (int) Math.min(Integer.MAX_VALUE, pageResult.getTotalElements());
        PaginationMeta meta = new PaginationMeta()
                .totalItems(totalItems)
                .totalPages(pageResult.getTotalPages())
                .currentPage(currentPage)
                .itemsPerPage(itemsPerPage);
        return new PaginatedRoutesResponse()
                .items(routeMapper.toRoutePreviewResponseList(pageResult.getContent()))
                .meta(meta);
    }

    @Transactional(readOnly = true)
    public RouteFullResponse getRouteById(UUID id) {
        TourRoute route = findByRouteId(id);
        return routeMapper.toRouteFullResponse(route);
    }

    @Transactional
    public UUID updateRouteMeta(UpdateRouteRequest request) {
        if (request.getUuid() == null) {
            throw new BadRequestException("Route id is required");
        }
        TourRoute route = findByRouteId(request.getUuid());
        validateRouteAccess(route);
        route.setTitle(request.getTitle());
        route.setDescription(request.getDescription() == null ? "" : request.getDescription());
        TourRoute updatedRoute = tourRouteRepository.save(route);
        return updatedRoute.getId();
    }

    @Transactional
    public void deleteRoute(UUID id) {
        TourRoute route = findByRouteId(id);
        validateRouteAccess(route);
        tourRouteRepository.delete(route);
    }

    private void validateLocations(List<LocationDto> locations) {
        if (locations == null || locations.size() < 2) {
            throw new BadRequestException("At least 2 locations are required");
        }
        long uniqueCount = locations.stream()
                .map(LocationDto::getOrderIndex)
                .distinct()
                .count();
        if (uniqueCount != locations.size()) {
            throw new BadRequestException("Location orderIndex values must be unique");
        }
    }

    private TourRoute findByRouteId(UUID routeId) {
        if (routeId == null) {
            throw new RouteNotFoundException(null);
        }
        return tourRouteRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException(routeId));
    }

    private void validateRouteAccess(TourRoute route) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        boolean isOwner = currentUserId != null
                && route.getAuthor() != null
                && currentUserId.equals(route.getAuthor().getId());
        boolean isAdmin = currentUserService.hasAdminRole();
        if (!isOwner && !isAdmin) {
            throw new RouteAccessDeniedException();
        }
    }

    private User resolveAuthor(UUID authorId) {
        if (authorId != null) {
            return userRepository.findById(authorId).orElseThrow(() -> new UserNotFoundException(authorId));
        }
        throw new RouteAccessDeniedException();
    }
}
