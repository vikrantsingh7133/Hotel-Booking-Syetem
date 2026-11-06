package org.example.hotelbookingsyetem.pricing;

import lombok.RequiredArgsConstructor;
import org.example.hotelbookingsyetem.model.DateRange;
import org.example.hotelbookingsyetem.model.RoomPriceOverride;
import org.example.hotelbookingsyetem.model.RoomType;
import org.example.hotelbookingsyetem.repository.RoomPriceOverrideRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/** Night price = override for that date if present, else the room type's base price. */
@Component
@RequiredArgsConstructor
public class DateOverridePricingStrategy implements PricingStrategy {

    private final RoomPriceOverrideRepository overrideRepository;

    @Override
    public SortedMap<LocalDate, BigDecimal> nightlyPrices(RoomType roomType, DateRange stay) {
        // Start with the base price for every night of the stay.
        SortedMap<LocalDate, BigDecimal> prices = new TreeMap<>();
        for (LocalDate night = stay.getCheckIn(); night.isBefore(stay.getCheckOut()); night = night.plusDays(1)) {
            prices.put(night, roomType.getBasePrice());
        }

        // Replace it on dates that have a special price.
        List<RoomPriceOverride> overrides = overrideRepository
                .findByRoomTypeIdAndDateBetween(roomType.getId(), stay.getCheckIn(), stay.lastNight());
        for (RoomPriceOverride override : overrides) {
            prices.put(override.getDate(), override.getPrice());
        }
        return prices;
    }
}
