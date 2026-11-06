package org.example.hotelbookingsyetem.pricing;

import org.example.hotelbookingsyetem.model.DateRange;
import org.example.hotelbookingsyetem.model.RoomType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.SortedMap;

public interface PricingStrategy {

    /** Price of each night in the stay, ordered by date. */
    SortedMap<LocalDate, BigDecimal> nightlyPrices(RoomType roomType, DateRange stay);

    /** Price of one room for the whole stay. */
    default BigDecimal totalPrice(RoomType roomType, DateRange stay) {
        return nightlyPrices(roomType, stay).values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Price of {@code rooms} identical rooms for the whole stay. */
    default BigDecimal totalPrice(RoomType roomType, DateRange stay, int rooms) {
        return totalPrice(roomType, stay).multiply(BigDecimal.valueOf(rooms));
    }
}
