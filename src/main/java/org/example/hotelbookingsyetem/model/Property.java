package org.example.hotelbookingsyetem.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.hotelbookingsyetem.exception.InvalidRequestException;
import org.example.hotelbookingsyetem.exception.NotFoundException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "properties")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String ownerId;

    @Column(nullable = false)
    private String name;

    @Embedded
    private Location location;

    /** 1.0 to 5.0, one decimal place, e.g. 4.5. */
    @Column(precision = 2, scale = 1)
    private BigDecimal starRating;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "property_amenities", joinColumns = @JoinColumn(name = "property_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "amenity")
    private Set<Amenity> amenities = new HashSet<>();

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RoomType> roomTypes = new ArrayList<>();

    private static final BigDecimal MIN_RATING = BigDecimal.ONE;
    private static final BigDecimal MAX_RATING = new BigDecimal("5");

    public Property(String ownerId, String name, Location location, BigDecimal starRating, Set<Amenity> amenities) {
        validateStarRating(starRating);
        this.ownerId = ownerId;
        this.name = name;
        this.location = location;
        this.starRating = starRating.setScale(1);
        this.amenities = amenities == null ? new HashSet<>() : new HashSet<>(amenities);
    }

    private static void validateStarRating(BigDecimal rating) {
        if (rating == null || rating.compareTo(MIN_RATING) < 0 || rating.compareTo(MAX_RATING) > 0) {
            throw new InvalidRequestException("starRating must be between 1.0 and 5.0");
        }
        if (rating.stripTrailingZeros().scale() > 1) {
            throw new InvalidRequestException("starRating can have at most one decimal place, e.g. 4.5");
        }
    }

    public void addRoomType(RoomType roomType) {
        roomType.assignTo(this);
        roomTypes.add(roomType);
    }

    public RoomType getRoomType(String roomTypeId) {
        for (RoomType roomType : roomTypes) {
            if (roomType.getId().equals(roomTypeId)) {
                return roomType;
            }
        }
        throw new NotFoundException("RoomType", roomTypeId);
    }

    public boolean hasAllAmenities(Set<Amenity> wanted) {
        return amenities.containsAll(wanted);
    }

    public Set<Amenity> getAmenities() {
        return amenities.isEmpty() ? EnumSet.noneOf(Amenity.class) : EnumSet.copyOf(amenities);
    }

    public List<RoomType> getRoomTypes() {
        return Collections.unmodifiableList(roomTypes);
    }
}
