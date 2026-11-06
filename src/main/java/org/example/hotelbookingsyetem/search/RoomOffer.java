package org.example.hotelbookingsyetem.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.hotelbookingsyetem.model.RoomType;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class RoomOffer {

    private final RoomType roomType;
    private final int availableRooms;
    private final int roomsNeeded;
    private final BigDecimal totalPrice;
    private final BigDecimal avgPricePerNight;
}
