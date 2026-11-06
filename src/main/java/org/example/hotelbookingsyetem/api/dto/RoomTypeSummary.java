package org.example.hotelbookingsyetem.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.hotelbookingsyetem.model.RoomType;

@Getter
@AllArgsConstructor
public class RoomTypeSummary {

    private final String id;
    private final String name;

    public static RoomTypeSummary from(RoomType roomType) {
        return new RoomTypeSummary(roomType.getId(), roomType.getName());
    }
}
