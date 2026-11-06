package org.example.hotelbookingsyetem.service;

import lombok.RequiredArgsConstructor;
import org.example.hotelbookingsyetem.exception.NotFoundException;
import org.example.hotelbookingsyetem.model.Booking;
import org.example.hotelbookingsyetem.model.BookingStatus;
import org.example.hotelbookingsyetem.model.Property;
import org.example.hotelbookingsyetem.repository.PropertyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Adds property and room type info to bookings, for showing them to the user.
 */
@Service
@RequiredArgsConstructor
public class BookingDetailsService {

    private final BookingService bookingService;
    private final PropertyRepository propertyRepository;

    public BookingDetails get(String bookingId) {
        return toDetails(bookingService.get(bookingId));
    }

    public Page<BookingDetails> historyForUser(String userId, BookingStatus status, int page, int size) {
        Page<Booking> bookings = bookingService.historyForUser(userId, status, page, size);
        List<BookingDetails> details = toDetails(bookings.getContent());
        return new PageImpl<>(details, bookings.getPageable(), bookings.getTotalElements());
    }

    public BookingDetails toDetails(Booking booking) {
        Property property = propertyRepository.findById(booking.getPropertyId())
                .orElseThrow(() -> new NotFoundException("Property", booking.getPropertyId()));
        return new BookingDetails(booking, property, property.getRoomType(booking.getRoomTypeId()));
    }

    public List<BookingDetails> toDetails(List<Booking> bookings) {
        // Fetch all properties in one query, not one query per booking.
        Set<String> propertyIds = new HashSet<>();
        for (Booking booking : bookings) {
            propertyIds.add(booking.getPropertyId());
        }
        Map<String, Property> propertyById = new HashMap<>();
        for (Property property : propertyRepository.findAllById(propertyIds)) {
            propertyById.put(property.getId(), property);
        }

        List<BookingDetails> result = new ArrayList<>();
        for (Booking booking : bookings) {
            Property property = propertyById.get(booking.getPropertyId());
            result.add(new BookingDetails(booking, property, property.getRoomType(booking.getRoomTypeId())));
        }
        return result;
    }
}
