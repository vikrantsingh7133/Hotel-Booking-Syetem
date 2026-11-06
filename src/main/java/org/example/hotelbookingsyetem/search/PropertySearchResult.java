package org.example.hotelbookingsyetem.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.hotelbookingsyetem.model.Property;

import java.util.List;

@Getter
@AllArgsConstructor
public class PropertySearchResult {

    private final Property property;
    private final List<RoomOffer> offers;
}
