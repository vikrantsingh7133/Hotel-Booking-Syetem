package org.example.hotelbookingsyetem.service;

import lombok.RequiredArgsConstructor;
import org.example.hotelbookingsyetem.model.DateRange;
import org.example.hotelbookingsyetem.model.RoomPriceOverride;
import org.example.hotelbookingsyetem.model.RoomType;
import org.example.hotelbookingsyetem.exception.InvalidRequestException;
import org.example.hotelbookingsyetem.exception.NotFoundException;
import org.example.hotelbookingsyetem.pricing.PricingStrategy;
import org.example.hotelbookingsyetem.repository.RoomPriceOverrideRepository;
import org.example.hotelbookingsyetem.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.SortedMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Manages per-date price overrides. Ranges here include both {@code from} and {@code to}. */
@Service
@RequiredArgsConstructor
public class PriceService {

    private final RoomTypeRepository roomTypeRepository;
    private final RoomPriceOverrideRepository overrideRepository;
    private final PricingStrategy pricingStrategy;

    @Transactional
    public SortedMap<LocalDate, BigDecimal> setPrice(String roomTypeId, LocalDate from, LocalDate to, BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new InvalidRequestException("price must be positive");
        }
        RoomType roomType = getRoomType(roomTypeId);
        DateRange range = DateRange.inclusive(from, to);

        Map<LocalDate, RoomPriceOverride> existing = overrideRepository
                .findByRoomTypeIdAndDateBetween(roomTypeId, from, to).stream()
                .collect(Collectors.toMap(RoomPriceOverride::getDate, Function.identity()));

        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            RoomPriceOverride override = existing.get(date);
            if (override != null) {
                override.changePrice(price);   // saved at commit by dirty checking
            } else {
                overrideRepository.save(new RoomPriceOverride(roomTypeId, date, price));
            }
        }
        return pricingStrategy.nightlyPrices(roomType, range);
    }

    @Transactional
    public void removePrices(String roomTypeId, LocalDate from, LocalDate to) {
        getRoomType(roomTypeId);
        DateRange.inclusive(from, to);
        overrideRepository.deleteByRoomTypeIdAndDateBetween(roomTypeId, from, to);
    }

    public SortedMap<LocalDate, BigDecimal> dailyPrices(String roomTypeId, LocalDate from, LocalDate to) {
        return pricingStrategy.nightlyPrices(getRoomType(roomTypeId), DateRange.inclusive(from, to));
    }

    private RoomType getRoomType(String roomTypeId) {
        return roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new NotFoundException("RoomType", roomTypeId));
    }
}
