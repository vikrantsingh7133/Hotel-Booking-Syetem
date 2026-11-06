package org.example.hotelbookingsyetem.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Sets {@code price} on every date from {@code from} to {@code to}, both included. */
@Getter
@Setter
@NoArgsConstructor
public class SetPriceRequest {

    @NotNull
    private LocalDate from;

    @NotNull
    private LocalDate to;

    @NotNull
    private BigDecimal price;
}
