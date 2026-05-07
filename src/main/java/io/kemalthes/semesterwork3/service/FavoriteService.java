package io.kemalthes.semesterwork3.service;

import io.kemalthes.core.dto.PaginatedRoutesResponse;
import io.kemalthes.core.dto.PaginationMeta;
import io.kemalthes.core.dto.RoutePreviewResponse;
import io.kemalthes.semesterwork3.entity.Favorite;
import io.kemalthes.semesterwork3.entity.TourRoute;
import io.kemalthes.semesterwork3.entity.User;
import io.kemalthes.semesterwork3.entity.enums.RouteStatus;
import io.kemalthes.semesterwork3.exception.AuthenticationRequiredException;
import io.kemalthes.semesterwork3.exception.ServiceException;
import io.kemalthes.semesterwork3.exception.RouteNotFoundException;
import io.kemalthes.semesterwork3.mapper.RouteMapper;
import io.kemalthes.semesterwork3.repository.FavoriteRepository;
import io.kemalthes.semesterwork3.repository.TourRouteRepository;
import io.kemalthes.semesterwork3.repository.UserRepository;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final CurrentUserService currentUserService;
    private final RouteMapper routeMapper;
    private final MinioService minioService;
    private final TourRouteRepository tourRouteRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PaginatedRoutesResponse getFavorites(@Min(1) Integer page, @Min(1) @Max(100) Integer limit) {
        int currentPage = page == null ? 1 : page;
        int itemsPerPage = limit == null ? 10 : limit;
        UUID userId = currentUserService.getCurrentUserId();
        if (userId == null) {
            throw new AuthenticationRequiredException();
        }
        Pageable pageable = PageRequest.of(currentPage - 1, itemsPerPage, Sort.by(Sort.Direction.ASC, "id"));
        Page<Favorite> pageResult = favoriteRepository.findAllByUserId(userId, pageable);
        int totalItems = (int) Math.min(Integer.MAX_VALUE, pageResult.getTotalElements());
        PaginationMeta meta = new PaginationMeta()
                .totalItems(totalItems)
                .totalPages(pageResult.getTotalPages())
                .currentPage(currentPage)
                .itemsPerPage(itemsPerPage);
        List<RoutePreviewResponse> routes = pageResult.getContent()
                .stream()
                .map(Favorite::getRoute)
                .map(route -> {
                    String presignedUrl = minioService.getPresignedUrl(route.getImageUrl());
                    return routeMapper.toRoutePreviewResponse(route, presignedUrl, true);
                })
                .toList();
        return new PaginatedRoutesResponse()
                .items(routes)
                .meta(meta);
    }

    @Transactional
    public void addRouteToFavorites(UUID routeId) {
        UUID userId = currentUserService.getCurrentUserId();
        if (userId == null) {
            throw new AuthenticationRequiredException();
        }
        if (favoriteRepository.existsByUserIdAndRouteId(userId, routeId)) {
            throw new ServiceException("Route is already in favorites", 409);
        }
        Favorite favorite = new Favorite();
        favorite.setId(UUID.randomUUID());
        TourRoute route = tourRouteRepository.findByIdAndStatus(routeId, RouteStatus.PUBLISHED).orElseThrow(
                () -> new RouteNotFoundException("Route with ID %s not found or not published".formatted(routeId))
        );
        User user = userRepository.getReferenceById(userId);
        favorite.setRoute(route);
        favorite.setUser(user);
        favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeRouteFromFavorites(UUID routeId) {
        UUID userId = currentUserService.getCurrentUserId();
        if (userId == null) {
            throw new AuthenticationRequiredException();
        }
        favoriteRepository.deleteByUserIdAndRouteId(userId, routeId);
    }
}
