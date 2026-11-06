package org.example.hotelbookingsyetem.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.hotelbookingsyetem.model.RoomType;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class RoomTypeResponse {

    private final String id;
    private final String name;
    private final int maxGuests;
    private final int totalRooms;
    private final BigDecimal basePrice;

    public static RoomTypeResponse from(RoomType roomType) {
        return new RoomTypeResponse(roomType.getId(), roomType.getName(), roomType.getMaxGuests(),
                roomType.getTotalRooms(), roomType.getBasePrice());
    }
}
