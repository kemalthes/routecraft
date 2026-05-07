package io.kemalthes.semesterwork3.entity;

import io.kemalthes.semesterwork3.entity.enums.RouteStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Builder;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tour_routes")
@Builder
public class TourRoute {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Version
    private Long version;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "distance", precision = 12, scale = 3)
    private BigDecimal distance;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status")
    private RouteStatus status;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @JdbcTypeCode(Types.LONGVARCHAR)
    @Column(name = "geometry", columnDefinition = "TEXT")
    private String geometry;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @OrderBy("orderIndex ASC")
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Location> locations = new ArrayList<>();

    @OneToMany(mappedBy = "route", cascade = CascadeType.REMOVE)
    @Builder.Default
    private Set<Favorite> favorites = new HashSet<>();

    @OrderBy("createdAt DESC")
    @OneToMany(mappedBy = "route", cascade = CascadeType.REMOVE)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();
}
