package org.example.hotelbookingsyetem.search;

import lombok.Getter;
import org.example.hotelbookingsyetem.exception.InvalidRequestException;
import org.example.hotelbookingsyetem.model.Amenity;
import org.example.hotelbookingsyetem.model.DateRange;
import org.example.hotelbookingsyetem.model.RoomType;

import java.math.BigDecimal;
import java.util.Set;

/** Optional fields are null (or empty) when the guest does not filter on them. */
@Getter
public class SearchCriteria {

    private final String city;
    private final String locality;
    private final DateRange stay;
    private final int guests;
    /** Rooms wanted; null means "as few as fit the guests" per room type. */
    private final Integer rooms;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final Set<Amenity> amenities;
    private final BigDecimal minStarRating;

    public SearchCriteria(String city, String locality, DateRange stay, int guests, Integer rooms,
                          BigDecimal minPrice, BigDecimal maxPrice,
                          Set<Amenity> amenities, BigDecimal minStarRating) {
        if (city == null || city.isBlank()) {
            throw new InvalidRequestException("city is required");
        }
        if (guests < 1) {
            throw new InvalidRequestException("guests must be at least 1");
        }
        if (rooms != null && (rooms < 1 || rooms > guests)) {
            throw new InvalidRequestException("rooms must be between 1 and the number of guests");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new InvalidRequestException("minPrice must not exceed maxPrice");
        }
        this.city = city;
        this.locality = locality;
        this.stay = stay;
        this.guests = guests;
        this.rooms = rooms;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.amenities = amenities == null ? Set.of() : Set.copyOf(amenities);
        this.minStarRating = minStarRating;
    }

    public int roomsFor(RoomType roomType) {
        return rooms != null ? rooms : roomType.roomsNeededFor(guests);
    }
}
