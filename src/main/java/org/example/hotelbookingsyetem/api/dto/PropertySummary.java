package org.example.hotelbookingsyetem.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.hotelbookingsyetem.model.Location;
import org.example.hotelbookingsyetem.model.Property;

/** Just enough of a property to show it on a booking. */
@Getter
@AllArgsConstructor
public class PropertySummary {

    private final String id;
    private final String name;
    private final String city;
    private final String locality;
    private final String address;

    public static PropertySummary from(Property property) {
        Location location = property.getLocation();
        return new PropertySummary(property.getId(), property.getName(),
                location.getCity(), location.getLocality(), location.getAddress());
    }
}
