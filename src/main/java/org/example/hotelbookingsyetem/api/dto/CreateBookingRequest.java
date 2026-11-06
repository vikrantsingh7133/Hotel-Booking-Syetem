package org.example.hotelbookingsyetem.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CreateBookingRequest {

    @NotBlank
    private String userId;

    @NotBlank
    private String propertyId;

    @NotBlank
    private String roomTypeId;

    @Min(1)
    private int guests;

    /** Rooms of the chosen type; defaults to 1. */
    @Min(1)
    private int rooms = 1;

    @NotNull
    private LocalDate checkIn;

    @NotNull
    private LocalDate checkOut;
}
