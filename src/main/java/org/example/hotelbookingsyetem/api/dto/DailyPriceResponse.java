package org.example.hotelbookingsyetem.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.SortedMap;

@Getter
@AllArgsConstructor
public class DailyPriceResponse {

    private final LocalDate date;
    private final BigDecimal price;

    public static List<DailyPriceResponse> from(SortedMap<LocalDate, BigDecimal> prices) {
        return prices.entrySet().stream()
                .map(e -> new DailyPriceResponse(e.getKey(), e.getValue()))
                .toList();
    }
}
