package org.example.hotelbookingsyetem.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.hotelbookingsyetem.model.RoomType;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class RoomTypeRequest {

    @NotBlank
    private String name;

    private int maxGuests;

    private int totalRooms;

    @NotNull
    private BigDecimal basePrice;

    public RoomType toDomain() {
        return new RoomType(name, maxGuests, totalRooms, basePrice);
    }
}
