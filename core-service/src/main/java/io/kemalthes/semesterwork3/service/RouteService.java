package io.kemalthes.semesterwork3.service;

import io.kemalthes.core.dto.CreateRouteRequest;
import io.kemalthes.core.dto.CreateRouteResponse;
import io.kemalthes.core.dto.DeleteRouteRequest;
import io.kemalthes.core.dto.LocationDto;
import io.kemalthes.core.dto.PaginatedRoutesResponse;
import io.kemalthes.core.dto.PaginationMeta;
import io.kemalthes.core.dto.RouteFullResponse;
import io.kemalthes.core.dto.UpdateRouteRequest;
import io.kemalthes.semesterwork3.dto.OsrmRouteMetrics;
import io.kemalthes.semesterwork3.entity.TourRoute;
import io.kemalthes.semesterwork3.entity.User;
import io.kemalthes.semesterwork3.entity.enums.RouteStatus;
import io.kemalthes.semesterwork3.exception.AuthenticationRequiredException;
import io.kemalthes.semesterwork3.exception.BadRequestException;
import io.kemalthes.semesterwork3.exception.RouteAccessDeniedException;
import io.kemalthes.semesterwork3.exception.RouteNotFoundException;
import io.kemalthes.semesterwork3.exception.UserNotFoundException;
import io.kemalthes.semesterwork3.mapper.LocationMapper;
import io.kemalthes.semesterwork3.mapper.RouteMapper;
import io.kemalthes.semesterwork3.repository.FavoriteRepository;
import io.kemalthes.semesterwork3.repository.TourRouteRepository;
import io.kemalthes.semesterwork3.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
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
    private final MinioService minioService;
    private final FavoriteRepository favoriteRepository;

    @Transactional
    public CreateRouteResponse createRoute(CreateRouteRequest request) {
        validateLocations(request.getLocations());
        UUID authorId = currentUserService.getCurrentUserId();
        User author = resolveAuthor(authorId);
        OsrmRouteMetrics metrics = osrmService.calculateRoute(request.getLocations());
        String objectKey = minioService.generateObjectName(request.getImageUrl());
        TourRoute route = TourRoute.builder()
                .id(UUID.randomUUID())
                .title(request.getTitle())
                .description(request.getDescription() == null ? "" : request.getDescription())
                .imageUrl(objectKey)
                .distance(BigDecimal.valueOf(metrics.distanceKm()))
                .status(RouteStatus.DRAFT)
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
        String presignedUrl = minioService.putPresignedUrl(objectKey);
        return new CreateRouteResponse(savedRoute.getId(), presignedUrl);
    }

    @Transactional(readOnly = true)
    public PaginatedRoutesResponse getRoutes(Integer page, Integer limit, String search) {
        int currentPage = page == null ? 1 : page;
        int itemsPerPage = limit == null ? 10 : limit;
        String query = search == null ? null : search.trim();
        Pageable pageable = PageRequest.of(currentPage - 1, itemsPerPage, Sort.by(Sort.Direction.ASC, "id"));
        Page<TourRoute> pageResult;
        if (query == null || query.isEmpty()) {
            pageResult = tourRouteRepository.findAllByStatus(RouteStatus.PUBLISHED, pageable);
        } else {
            pageResult = tourRouteRepository.searchByTitleOrDescription(query, RouteStatus.PUBLISHED, pageable);
        }
        int totalItems = (int) Math.min(Integer.MAX_VALUE, pageResult.getTotalElements());
        PaginationMeta meta = new PaginationMeta()
                .totalItems(totalItems)
                .totalPages(pageResult.getTotalPages())
                .currentPage(currentPage)
                .itemsPerPage(itemsPerPage);
        return new PaginatedRoutesResponse()
                .items(pageResult.getContent().stream()
                        .map(route -> {
                            String presignedUrl = minioService.getPresignedUrl(route.getImageUrl());
                            return routeMapper.toRoutePreviewResponse(route, presignedUrl, isRouteLikedByCurrentUser(route.getId()));
                        })
                        .toList())
                .meta(meta);
    }

    @Transactional(readOnly = true)
    public PaginatedRoutesResponse getMyRoutes(Integer page, Integer limit) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        if (currentUserId == null) {
            throw new AuthenticationRequiredException();
        }
        Pageable pageable = PageRequest.of(
                (page == null ? 1 : page) - 1,
                limit == null ? 10 : limit,
                Sort.by(Sort.Direction.ASC, "id")
        );
        Page<TourRoute> pageResult = tourRouteRepository.findAllByAuthorId(currentUserId, pageable);
        int currentPage = page == null ? 1 : page;
        int itemsPerPage = limit == null ? 10 : limit;
        int totalItems = (int) Math.min(Integer.MAX_VALUE, pageResult.getTotalElements());
        PaginationMeta meta = new PaginationMeta()
                .totalItems(totalItems)
                .totalPages(pageResult.getTotalPages())
                .currentPage(currentPage)
                .itemsPerPage(itemsPerPage);
        return new PaginatedRoutesResponse()
                .items(pageResult.getContent().stream()
                        .map(route -> {
                            String presignedUrl = minioService.getPresignedUrl(route.getImageUrl());
                            return routeMapper.toRoutePreviewResponse(
                                    route,
                                    presignedUrl,
                                    isRouteLikedByCurrentUser(route.getId())
                            );
                        })
                        .toList())
                .meta(meta);
    }

    @Transactional(readOnly = true)
    public PaginatedRoutesResponse getAdminRoutes(Integer page, Integer limit, RouteStatus status) {
        validateAdminAccess();
        Pageable pageable = PageRequest.of(
                (page == null ? 1 : page) - 1,
                limit == null ? 10 : limit,
                Sort.by(Sort.Direction.ASC, "id")
        );
        Page<TourRoute> pageResult = status == null
                ? tourRouteRepository.findAll(pageable)
                : tourRouteRepository.findAllByStatus(status, pageable);
        int currentPage = page == null ? 1 : page;
        int itemsPerPage = limit == null ? 10 : limit;
        int totalItems = (int) Math.min(Integer.MAX_VALUE, pageResult.getTotalElements());
        PaginationMeta meta = new PaginationMeta()
                .totalItems(totalItems)
                .totalPages(pageResult.getTotalPages())
                .currentPage(currentPage)
                .itemsPerPage(itemsPerPage);
        return new PaginatedRoutesResponse()
                .items(pageResult.getContent().stream()
                        .map(route -> {
                            String presignedUrl = minioService.getPresignedUrl(route.getImageUrl());
                            return routeMapper.toRoutePreviewResponse(
                                    route,
                                    presignedUrl,
                                    isRouteLikedByCurrentUser(route.getId())
                            );
                        })
                        .toList())
                .meta(meta);
    }

    @Transactional(readOnly = true)
    public RouteFullResponse getRouteById(UUID id) {
        if (id == null) throw new RouteNotFoundException(null);
        TourRoute route = tourRouteRepository.findByIdAndStatus(id, RouteStatus.PUBLISHED)
                .orElseThrow(() -> new RouteNotFoundException(id));
        String presignedUrl = minioService.getPresignedUrl(route.getImageUrl());
        return routeMapper.toRouteFullResponse(route, presignedUrl, isRouteLikedByCurrentUser(id));
    }

    @Transactional(readOnly = true)
    public RouteFullResponse getOwnUnpublishedRouteById(UUID id) {
        if (id == null) {
            throw new RouteNotFoundException(null);
        }
        TourRoute route = findByRouteId(id);
        validateOwnUnpublishedRouteAccess(route);
        String presignedUrl = minioService.getPresignedUrl(route.getImageUrl());
        return routeMapper.toRouteFullResponse(route, presignedUrl, false);
    }

    @Transactional
    public UUID confirmRoute(UpdateRouteRequest request) {
        if (request.getUuid() == null) {
            throw new BadRequestException("Route id is required");
        }
        validateAdminAccess();
        TourRoute route = findByRouteId(request.getUuid());
        validateExpectedVersion(route, request.getVersion());
        route.setTitle(request.getTitle());
        route.setDescription(request.getDescription() == null ? "" : request.getDescription());
        route.setStatus(RouteStatus.PUBLISHED);
        TourRoute updatedRoute = tourRouteRepository.save(route);
        return updatedRoute.getId();
    }

    @Transactional
    public UUID updateRoute(@Valid UpdateRouteRequest request) {
        if (request.getUuid() == null) {
            throw new BadRequestException("Route id is required");
        }
        TourRoute route = findByRouteId(request.getUuid());
        validateOwnUnpublishedRouteAccess(route);
        validateExpectedVersion(route, request.getVersion());
        if (route.getStatus() == RouteStatus.PUBLISHED) {
            throw new RouteAccessDeniedException();
        }
        route.setTitle(request.getTitle());
        route.setDescription(request.getDescription() == null ? "" : request.getDescription());
        return tourRouteRepository.save(route).getId();
    }

    @Transactional
    public void deleteOwnUnpublishedRoute(UUID id, Long version) {
        TourRoute route = findByRouteId(id);
        validateOwnUnpublishedRouteAccess(route);
        validateExpectedVersion(route, version);
        tourRouteRepository.delete(route);
    }

    @Transactional
    public void deleteRouteAdmin(DeleteRouteRequest request) {
        validateAdminAccess();
        TourRoute route = findByRouteId(request.getUuid());
        validateExpectedVersion(route, request.getVersion());
        tourRouteRepository.delete(route);
    }

    @Transactional
    public void setStatusPending(String objectKey) {
        TourRoute route = tourRouteRepository.findByImageUrl(objectKey).orElseThrow(() -> new BadRequestException("Invalid object key"));
        route.setStatus(RouteStatus.PENDING);
        tourRouteRepository.save(route);
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

    private void validateOwnUnpublishedRouteAccess(TourRoute route) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        boolean isOwner = currentUserId != null
                && route.getAuthor() != null
                && currentUserId.equals(route.getAuthor().getId());
        if (!isOwner || route.getStatus() == RouteStatus.PUBLISHED) {
            throw new RouteAccessDeniedException();
        }
    }

    private void validateAdminAccess() {
        if (!currentUserService.hasAdminRole()) {
            throw new RouteAccessDeniedException();
        }
    }

    private void validateExpectedVersion(TourRoute route, Long expectedVersion) {
        if (expectedVersion != null && !expectedVersion.equals(route.getVersion())) {
            throw new OptimisticLockingFailureException("Route was modified by another user");
        }
    }

    private boolean isRouteLikedByCurrentUser(UUID routeId) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        return currentUserId != null && favoriteRepository.existsByUserIdAndRouteId(currentUserId, routeId);
    }

    private User resolveAuthor(UUID authorId) {
        if (authorId != null) {
            return userRepository.findById(authorId).orElseThrow(() -> new UserNotFoundException(authorId));
        }
        throw new RouteAccessDeniedException();
    }
}
