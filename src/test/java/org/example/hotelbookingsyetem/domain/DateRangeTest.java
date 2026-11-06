package org.example.hotelbookingsyetem.domain;

import org.example.hotelbookingsyetem.exception.InvalidRequestException;
import org.example.hotelbookingsyetem.model.DateRange;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateRangeTest {

    private static final LocalDate DAY = LocalDate.of(2030, 1, 10);

    @Test
    void countsNightsWithoutCheckoutDay() {
        DateRange stay = new DateRange(DAY, DAY.plusDays(3));

        assertThat(stay.nights()).isEqualTo(3);
        assertThat(stay.nightDates()).containsExactly(DAY, DAY.plusDays(1), DAY.plusDays(2));
        assertThat(stay.includesNight(DAY.plusDays(3))).isFalse();
    }

    @Test
    void rejectsCheckoutOnOrBeforeCheckin() {
        assertThatThrownBy(() -> new DateRange(DAY, DAY)).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> new DateRange(DAY, DAY.minusDays(1))).isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void inclusiveRangeOfOneDayHasOneNight() {
        assertThat(DateRange.inclusive(DAY, DAY).nights()).isEqualTo(1);
    }
}
