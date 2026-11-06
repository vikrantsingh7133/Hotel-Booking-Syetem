package org.example.hotelbookingsyetem.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.hotelbookingsyetem.model.Amenity;
import org.example.hotelbookingsyetem.model.Property;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class CreatePropertyRequest {

    @NotBlank
    private String name;

    @NotNull
    @Valid
    private LocationDto location;

    @NotNull
    private BigDecimal starRating;

    private Set<Amenity> amenities;

    @NotEmpty
    private List<@Valid RoomTypeRequest> roomTypes;

    public Property toDomain(String ownerId) {
        Property property = new Property(ownerId, name, location.toDomain(), starRating, amenities);
        roomTypes.forEach(roomType -> property.addRoomType(roomType.toDomain()));
        return property;
    }
}
