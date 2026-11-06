package org.example.hotelbookingsyetem.pricing;

import org.example.hotelbookingsyetem.model.DateRange;
import org.example.hotelbookingsyetem.model.RoomPriceOverride;
import org.example.hotelbookingsyetem.model.RoomType;
import org.example.hotelbookingsyetem.repository.RoomPriceOverrideRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DateOverridePricingStrategyTest {

    private static final LocalDate DEC_30 = LocalDate.of(2030, 12, 30);
    private static final LocalDate DEC_31 = LocalDate.of(2030, 12, 31);

    private final RoomPriceOverrideRepository repository = mock(RoomPriceOverrideRepository.class);
    private final DateOverridePricingStrategy strategy = new DateOverridePricingStrategy(repository);
    private final RoomType room = new RoomType("Deluxe", 2, 5, new BigDecimal("2000"));

    @Test
    void usesOverrideWhenPresentAndBasePriceOtherwise() {
        when(repository.findByRoomTypeIdAndDateBetween(any(), eq(DEC_30), eq(DEC_31)))
                .thenReturn(List.of(new RoomPriceOverride("room", DEC_31, new BigDecimal("5000"))));

        DateRange stay = new DateRange(DEC_30, DEC_31.plusDays(1));

        assertThat(strategy.nightlyPrices(room, stay))
                .containsEntry(DEC_30, new BigDecimal("2000"))
                .containsEntry(DEC_31, new BigDecimal("5000"));
        assertThat(strategy.totalPrice(room, stay)).isEqualByComparingTo("7000");
    }

    @Test
    void overrideCanBeLowerThanBase() {
        when(repository.findByRoomTypeIdAndDateBetween(any(), eq(DEC_30), eq(DEC_30)))
                .thenReturn(List.of(new RoomPriceOverride("room", DEC_30, new BigDecimal("1500"))));

        assertThat(strategy.totalPrice(room, new DateRange(DEC_30, DEC_31))).isEqualByComparingTo("1500");
    }
}
