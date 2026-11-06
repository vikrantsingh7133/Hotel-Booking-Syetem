package org.example.hotelbookingsyetem.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.example.hotelbookingsyetem.exception.InvalidRequestException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

/**
 * A stay: check-in day is a night, check-out day is not.
 */
@Getter
@EqualsAndHashCode
@ToString
public class DateRange {

    private final LocalDate checkIn;
    private final LocalDate checkOut;

    public DateRange(LocalDate checkIn, LocalDate checkOut) {
        requireDate(checkIn, "checkIn");
        requireDate(checkOut, "checkOut");
        if (!checkIn.isBefore(checkOut)) {
            throw new InvalidRequestException("checkIn must be before checkOut");
        }
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    /** Range where both from and to are nights, e.g. for price calendars. */
    public static DateRange inclusive(LocalDate from, LocalDate to) {
        requireDate(from, "from");
        requireDate(to, "to");
        if (from.isAfter(to)) {
            throw new InvalidRequestException("from must not be after to");
        }
        return new DateRange(from, to.plusDays(1));
    }

    public long nights() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public LocalDate lastNight() {
        return checkOut.minusDays(1);
    }

    public Stream<LocalDate> nightDates() {
        return checkIn.datesUntil(checkOut);
    }

    public boolean includesNight(LocalDate date) {
        return !date.isBefore(checkIn) && date.isBefore(checkOut);
    }

    private static void requireDate(LocalDate date, String name) {
        if (date == null) {
            throw new InvalidRequestException(name + " is required");
        }
    }
}
