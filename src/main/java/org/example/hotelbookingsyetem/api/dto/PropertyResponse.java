package org.example.hotelbookingsyetem.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.hotelbookingsyetem.model.Amenity;
import org.example.hotelbookingsyetem.model.Property;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Getter
@AllArgsConstructor
public class PropertyResponse {

    private final String id;
    private final String ownerId;
    private final String name;
    private final LocationDto location;
    private final BigDecimal starRating;
    private final Set<Amenity> amenities;
    private final List<RoomTypeResponse> roomTypes;

    public static PropertyResponse from(Property property) {
        return new PropertyResponse(
                property.getId(),
                property.getOwnerId(),
                property.getName(),
                LocationDto.from(property.getLocation()),
                property.getStarRating(),
                property.getAmenities(),
                property.getRoomTypes().stream().map(RoomTypeResponse::from).toList());
    }
}
