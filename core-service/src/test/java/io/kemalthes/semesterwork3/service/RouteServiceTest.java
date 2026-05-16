package io.kemalthes.semesterwork3.service;

import io.kemalthes.core.dto.CreateRouteRequest;
import io.kemalthes.core.dto.LocationDto;
import io.kemalthes.core.dto.RoutePreviewResponse;
import io.kemalthes.core.dto.UpdateRouteRequest;
import io.kemalthes.semesterwork3.dto.OsrmRouteMetrics;
import io.kemalthes.semesterwork3.entity.Location;
import io.kemalthes.semesterwork3.entity.TourRoute;
import io.kemalthes.semesterwork3.entity.User;
import io.kemalthes.semesterwork3.entity.enums.RouteStatus;
import io.kemalthes.semesterwork3.exception.BadRequestException;
import io.kemalthes.semesterwork3.exception.RouteAccessDeniedException;
import io.kemalthes.semesterwork3.mapper.LocationMapper;
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
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    private TourRouteRepository tourRouteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OsrmService osrmService;

    @Mock
    private RouteMapper routeMapper;

    @Mock
    private LocationMapper locationMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private MinioService minioService;

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private RouteSearchPublisher routeSearchPublisher;

    @InjectMocks
    private RouteService routeService;

    @Test
    void createRouteSavesDraftRouteAndReturnsUploadUrl() {
        UUID authorId = UUID.randomUUID();
        User author = user(authorId, "author");
        LocationDto second = new LocationDto(55.75, 37.61, 1);
        LocationDto first = new LocationDto(59.93, 30.31, 0);
        CreateRouteRequest request = new CreateRouteRequest()
                .title("City walk")
                .description(null)
                .imageUrl("routes/original.png")
                .locations(List.of(second, first));
        Location firstLocation = new Location();
        firstLocation.setOrderIndex(0);
        Location secondLocation = new Location();
        secondLocation.setOrderIndex(1);

        when(currentUserService.getCurrentUserId()).thenReturn(authorId);
        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(osrmService.calculateRoute(request.getLocations()))
                .thenReturn(new OsrmRouteMetrics(12.5, 34, "encoded-geometry"));
        when(minioService.generateObjectName("routes/original.png")).thenReturn("generated/object.png");
        when(locationMapper.toLocation(first)).thenReturn(firstLocation);
        when(locationMapper.toLocation(second)).thenReturn(secondLocation);
        when(tourRouteRepository.save(any(TourRoute.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(minioService.putPresignedUrl("generated/object.png")).thenReturn("http://minio/upload-url");

        var response = routeService.createRoute(request);

        ArgumentCaptor<TourRoute> routeCaptor = ArgumentCaptor.forClass(TourRoute.class);
        verify(tourRouteRepository).save(routeCaptor.capture());
        TourRoute savedRoute = routeCaptor.getValue();
        assertEquals(response.getUuid(), savedRoute.getId());
        assertEquals("http://minio/upload-url", response.getImageUrl());
        assertEquals("City walk", savedRoute.getTitle());
        assertEquals("", savedRoute.getDescription());
        assertEquals("generated/object.png", savedRoute.getImageUrl());
        assertEquals(RouteStatus.DRAFT, savedRoute.getStatus());
        assertEquals(BigDecimal.valueOf(12.5), savedRoute.getDistance());
        assertEquals(34, savedRoute.getDurationMinutes());
        assertEquals("encoded-geometry", savedRoute.getGeometry());
        assertEquals(author, savedRoute.getAuthor());
        assertEquals(List.of(firstLocation, secondLocation), savedRoute.getLocations());
        assertEquals(savedRoute, firstLocation.getRoute());
        assertEquals(savedRoute, secondLocation.getRoute());
    }

    @Test
    void createRouteRejectsDuplicateLocationOrderIndexes() {
        CreateRouteRequest request = new CreateRouteRequest()
                .title("City walk")
                .locations(List.of(
                        new LocationDto(55.75, 37.61, 0),
                        new LocationDto(59.93, 30.31, 0)
                ));

        assertThrows(BadRequestException.class, () -> routeService.createRoute(request));

        verifyNoInteractions(tourRouteRepository, osrmService, minioService);
    }

    @Test
    void getRoutesUsesSearchAndMarksLikedRoutes() {
        UUID userId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();
        TourRoute route = route(routeId, RouteStatus.PUBLISHED, user(UUID.randomUUID(), "author"));
        var page = new PageImpl<>(List.of(route));
        var preview = new RoutePreviewResponse().id(routeId).imageUrl("http://minio/read-url").isLiked(true);

        when(tourRouteRepository.searchByTitleOrDescription(eq("park"), eq(RouteStatus.PUBLISHED), any(Pageable.class)))
                .thenReturn(page);
        when(currentUserService.getCurrentUserId()).thenReturn(userId);
        when(favoriteRepository.findLikedRouteIds(userId, List.of(routeId))).thenReturn(List.of(routeId));
        when(minioService.getPresignedUrl(route.getImageUrl())).thenReturn("http://minio/read-url");
        when(routeMapper.toRoutePreviewResponse(route, "http://minio/read-url", true)).thenReturn(preview);

        var response = routeService.getRoutes(1, 10, " park ");

        assertEquals(1, response.getItems().size());
        assertEquals(preview, response.getItems().getFirst());
        assertEquals(1, response.getMeta().getCurrentPage());
        assertEquals(10, response.getMeta().getItemsPerPage());
        assertEquals(1, response.getMeta().getTotalItems());
        assertEquals(1, response.getMeta().getTotalPages());
    }

    @Test
    void updateRouteUpdatesOwnDraftWhenVersionMatches() {
        UUID ownerId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();
        TourRoute route = route(routeId, RouteStatus.DRAFT, user(ownerId, "owner"));
        route.setVersion(3L);
        UpdateRouteRequest request = new UpdateRouteRequest()
                .uuid(routeId)
                .version(3L)
                .title("Updated title")
                .description(null);

        when(tourRouteRepository.findById(routeId)).thenReturn(Optional.of(route));
        when(currentUserService.getCurrentUserId()).thenReturn(ownerId);
        when(tourRouteRepository.save(route)).thenReturn(route);

        UUID result = routeService.updateRoute(request);

        assertEquals(routeId, result);
        assertEquals("Updated title", route.getTitle());
        assertEquals("", route.getDescription());
        verify(tourRouteRepository).save(route);
    }

    @Test
    void updateRouteRejectsStaleVersion() {
        UUID ownerId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();
        TourRoute route = route(routeId, RouteStatus.DRAFT, user(ownerId, "owner"));
        route.setVersion(4L);
        UpdateRouteRequest request = new UpdateRouteRequest()
                .uuid(routeId)
                .version(3L)
                .title("Updated title");

        when(tourRouteRepository.findById(routeId)).thenReturn(Optional.of(route));
        when(currentUserService.getCurrentUserId()).thenReturn(ownerId);

        assertThrows(OptimisticLockingFailureException.class, () -> routeService.updateRoute(request));

        verify(tourRouteRepository, never()).save(any());
    }

    @Test
    void updateRouteRejectsNonOwner() {
        UUID routeId = UUID.randomUUID();
        TourRoute route = route(routeId, RouteStatus.DRAFT, user(UUID.randomUUID(), "owner"));
        UpdateRouteRequest request = new UpdateRouteRequest()
                .uuid(routeId)
                .title("Updated title");

        when(tourRouteRepository.findById(routeId)).thenReturn(Optional.of(route));
        when(currentUserService.getCurrentUserId()).thenReturn(UUID.randomUUID());

        assertThrows(RouteAccessDeniedException.class, () -> routeService.updateRoute(request));

        verify(tourRouteRepository, never()).save(any());
    }

    @Test
    void confirmRoutePublishesAndEnqueuesSearchIndexing() {
        UUID routeId = UUID.randomUUID();
        TourRoute route = route(routeId, RouteStatus.PENDING, user(UUID.randomUUID(), "author"));
        route.setVersion(7L);
        UpdateRouteRequest request = new UpdateRouteRequest()
                .uuid(routeId)
                .version(7L)
                .title("Published title")
                .description("Published description");

        when(currentUserService.hasAdminRole()).thenReturn(true);
        when(tourRouteRepository.findById(routeId)).thenReturn(Optional.of(route));
        when(tourRouteRepository.save(route)).thenReturn(route);

        UUID result = routeService.confirmRoute(request);

        assertEquals(routeId, result);
        assertEquals(RouteStatus.PUBLISHED, route.getStatus());
        assertEquals("Published title", route.getTitle());
        assertEquals("Published description", route.getDescription());
        verify(routeSearchPublisher).publishAfterCommit(route);
    }

    @Test
    void setStatusPendingUpdatesRouteByObjectKey() {
        TourRoute route = route(UUID.randomUUID(), RouteStatus.DRAFT, user(UUID.randomUUID(), "author"));
        when(tourRouteRepository.findByImageUrl("object.png")).thenReturn(Optional.of(route));

        routeService.setStatusPending("object.png");

        assertEquals(RouteStatus.PENDING, route.getStatus());
        verify(tourRouteRepository).save(route);
    }

    private static User user(UUID id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        return user;
    }

    private static TourRoute route(UUID id, RouteStatus status, User author) {
        return TourRoute.builder()
                .id(id)
                .title("Route title")
                .description("Route description")
                .imageUrl("image.png")
                .distance(BigDecimal.TEN)
                .durationMinutes(25)
                .geometry("geometry")
                .status(status)
                .author(author)
                .build();
    }
}
