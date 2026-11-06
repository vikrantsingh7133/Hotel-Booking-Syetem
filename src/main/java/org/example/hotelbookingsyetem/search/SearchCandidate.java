package org.example.hotelbookingsyetem.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.hotelbookingsyetem.model.Property;
import org.example.hotelbookingsyetem.model.RoomType;

import java.math.BigDecimal;

/** A priced room type (for {@code rooms} rooms) that filters decide to keep or drop. */
@Getter
@AllArgsConstructor
public class SearchCandidate {

    private final Property property;
    private final RoomType roomType;
    private final int rooms;
    /** All rooms, all nights. */
    private final BigDecimal totalPrice;
    /** One room, averaged over the nights. */
    private final BigDecimal avgPricePerNight;
}
