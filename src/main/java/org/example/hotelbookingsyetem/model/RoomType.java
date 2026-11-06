package org.example.hotelbookingsyetem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.hotelbookingsyetem.exception.InvalidRequestException;

import java.math.BigDecimal;

/** A kind of room in a property (e.g. Deluxe) with a count of identical rooms. */
@Entity
@Table(name = "room_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id")
    private Property property;

    @Column(nullable = false)
    private String name;

    private int maxGuests;

    private int totalRooms;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    public RoomType(String name, int maxGuests, int totalRooms, BigDecimal basePrice) {
        if (maxGuests < 1 || totalRooms < 1) {
            throw new InvalidRequestException("maxGuests and totalRooms must be at least 1");
        }
        if (basePrice == null || basePrice.signum() <= 0) {
            throw new InvalidRequestException("basePrice must be positive");
        }
        this.name = name;
        this.maxGuests = maxGuests;
        this.totalRooms = totalRooms;
        this.basePrice = basePrice;
    }

    void assignTo(Property property) {
        this.property = property;
    }

    public String getPropertyId() {
        return property.getId();
    }

    public boolean belongsTo(String propertyId) {
        return getPropertyId().equals(propertyId);
    }

    /** True if {@code rooms} rooms of this type can sleep {@code guests} people. */
    public boolean canHost(int guests, int rooms) {
        return guests <= rooms * maxGuests;
    }

    /** Fewest rooms of this type that fit {@code guests} people. */
    public int roomsNeededFor(int guests) {
        return (guests + maxGuests - 1) / maxGuests;
    }
}
