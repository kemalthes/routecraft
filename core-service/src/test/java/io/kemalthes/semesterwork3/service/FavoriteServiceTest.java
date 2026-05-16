package io.kemalthes.semesterwork3.service;

import io.kemalthes.core.dto.RoutePreviewResponse;
import io.kemalthes.semesterwork3.entity.Favorite;
import io.kemalthes.semesterwork3.entity.TourRoute;
import io.kemalthes.semesterwork3.entity.User;
import io.kemalthes.semesterwork3.entity.enums.RouteStatus;
import io.kemalthes.semesterwork3.exception.AuthenticationRequiredException;
import io.kemalthes.semesterwork3.exception.ServiceException;
import io.kemalthes.semesterwork3.mapper.RouteMapper;
import io.kemalthes.semesterwork3.repository.FavoriteRepository;
import io.kemalthes.semesterwork3.repository.TourRouteRepository;
import io.kemalthes.semesterwork3.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private RouteMapper routeMapper;

    @Mock
    private MinioService minioService;

    @Mock
    private TourRouteRepository tourRouteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    @Test
    void getFavoritesReturnsMappedLikedRoutes() {
        UUID userId = UUID.randomUUID();
        TourRoute route = route(UUID.randomUUID());
        Favorite favorite = new Favorite();
        favorite.setRoute(route);
        RoutePreviewResponse mapped = new RoutePreviewResponse().id(route.getId()).isLiked(true);

        when(currentUserService.getCurrentUserId()).thenReturn(userId);
        when(favoriteRepository.findAllByUserId(eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(favorite)));
        when(minioService.getPresignedUrl(route.getImageUrl())).thenReturn("http://minio/read-url");
        when(routeMapper.toRoutePreviewResponse(route, "http://minio/read-url", true)).thenReturn(mapped);

        var response = favoriteService.getFavorites(null, null);

        assertEquals(List.of(mapped), response.getItems());
        assertEquals(1, response.getMeta().getCurrentPage());
        assertEquals(10, response.getMeta().getItemsPerPage());
    }

    @Test
    void addRouteToFavoritesSavesFavoriteForCurrentUser() {
        UUID userId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();
        TourRoute route = route(routeId);
        User user = user(userId);

        when(currentUserService.getCurrentUserId()).thenReturn(userId);
        when(favoriteRepository.existsByUserIdAndRouteId(userId, routeId)).thenReturn(false);
        when(tourRouteRepository.findByIdAndStatus(routeId, RouteStatus.PUBLISHED)).thenReturn(Optional.of(route));
        when(userRepository.getReferenceById(userId)).thenReturn(user);

        favoriteService.addRouteToFavorites(routeId);

        ArgumentCaptor<Favorite> favoriteCaptor = ArgumentCaptor.forClass(Favorite.class);
        verify(favoriteRepository).save(favoriteCaptor.capture());
        Favorite savedFavorite = favoriteCaptor.getValue();
        assertEquals(user, savedFavorite.getUser());
        assertEquals(route, savedFavorite.getRoute());
    }

    @Test
    void addRouteToFavoritesRejectsAnonymousUser() {
        when(currentUserService.getCurrentUserId()).thenReturn(null);

        assertThrows(AuthenticationRequiredException.class,
                () -> favoriteService.addRouteToFavorites(UUID.randomUUID()));

        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void addRouteToFavoritesRejectsDuplicateFavorite() {
        UUID userId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();

        when(currentUserService.getCurrentUserId()).thenReturn(userId);
        when(favoriteRepository.existsByUserIdAndRouteId(userId, routeId)).thenReturn(true);

        assertThrows(ServiceException.class, () -> favoriteService.addRouteToFavorites(routeId));

        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void removeRouteFromFavoritesDeletesCurrentUsersFavorite() {
        UUID userId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();
        when(currentUserService.getCurrentUserId()).thenReturn(userId);

        favoriteService.removeRouteFromFavorites(routeId);

        verify(favoriteRepository).deleteByUserIdAndRouteId(userId, routeId);
    }

    private static TourRoute route(UUID id) {
        return TourRoute.builder()
                .id(id)
                .title("Route")
                .imageUrl("route.png")
                .status(RouteStatus.PUBLISHED)
                .author(user(UUID.randomUUID()))
                .build();
    }

    private static User user(UUID id) {
        User user = new User();
        user.setId(id);
        user.setUsername("user");
        user.setEmail("user@example.com");
        return user;
    }
}
