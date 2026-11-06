package org.example.hotelbookingsyetem.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.hotelbookingsyetem.model.Location;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {

    @NotBlank
    private String city;

    private String locality;

    private String address;

    public static LocationDto from(Location location) {
        return new LocationDto(location.getCity(), location.getLocality(), location.getAddress());
    }

    public Location toDomain() {
        return new Location(city, locality, address);
    }
}
