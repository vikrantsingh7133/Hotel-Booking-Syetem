package org.example.hotelbookingsyetem.service;

import lombok.RequiredArgsConstructor;
import org.example.hotelbookingsyetem.model.DateRange;
import org.example.hotelbookingsyetem.model.Property;
import org.example.hotelbookingsyetem.model.RoomType;
import org.example.hotelbookingsyetem.pricing.PricingStrategy;
import org.example.hotelbookingsyetem.repository.PropertyRepository;
import org.example.hotelbookingsyetem.search.PropertySearchResult;
import org.example.hotelbookingsyetem.search.RoomOffer;
import org.example.hotelbookingsyetem.search.SearchCandidate;
import org.example.hotelbookingsyetem.search.SearchCriteria;
import org.example.hotelbookingsyetem.search.filter.SearchFilter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final PropertyRepository propertyRepository;
    private final PricingStrategy pricingStrategy;
    private final AvailabilityService availabilityService;
    private final List<SearchFilter> filters;

    public List<PropertySearchResult> search(SearchCriteria criteria) {
        List<PropertySearchResult> results = new ArrayList<>();

        List<Property> propertyList = propertyRepository.findByLocationCityIgnoreCase(criteria.getCity());

        for (Property property : propertyList) {
            List<RoomOffer> offers = new ArrayList<>();
            for (RoomType roomType : property.getRoomTypes()) {
                RoomOffer offer = findOffer(property, roomType, criteria);
                if (offer != null) {
                    offers.add(offer);
                }
            }
            // Show a property only if at least one of its room types fits the search.
            if (!offers.isEmpty()) {
                results.add(new PropertySearchResult(property, offers));
            }
        }
        return results;
    }

    /** Returns null if this room type does not match the filters or has too few free rooms. */
    private RoomOffer findOffer(Property property, RoomType roomType, SearchCriteria criteria) {
        DateRange stay = criteria.getStay();
        int rooms = criteria.roomsFor(roomType);

        // 1. Price
        BigDecimal oneRoomPrice = pricingStrategy.totalPrice(roomType, stay);
        BigDecimal pricePerNight = oneRoomPrice.divide(BigDecimal.valueOf(stay.nights()), 2, RoundingMode.HALF_UP);
        BigDecimal totalPrice = oneRoomPrice.multiply(BigDecimal.valueOf(rooms));

        // 2. Filters: drop this room type if any filter says no
        SearchCandidate candidate = new SearchCandidate(property, roomType, rooms, totalPrice, pricePerNight);
        for (SearchFilter filter : filters) {
            if (!filter.matches(candidate, criteria)) {
                return null;
            }
        }

        // 3. Free rooms (last, because it queries the bookings table)
        int freeRooms = availabilityService.availableRooms(roomType, stay);
        if (freeRooms < rooms) {
            return null;
        }

        return new RoomOffer(roomType, freeRooms, rooms, totalPrice, pricePerNight);
    }
}
