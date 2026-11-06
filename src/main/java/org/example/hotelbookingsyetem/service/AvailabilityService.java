package org.example.hotelbookingsyetem.service;

import lombok.RequiredArgsConstructor;
import org.example.hotelbookingsyetem.model.Booking;
import org.example.hotelbookingsyetem.model.BookingStatus;
import org.example.hotelbookingsyetem.model.DateRange;
import org.example.hotelbookingsyetem.model.RoomType;
import org.example.hotelbookingsyetem.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Free rooms are derived from bookings that hold a room, so there is no
 * separate counter that can drift. Cancelling or failing a booking frees the room.
 */
@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final BookingRepository bookingRepository;

    /** Rooms free on every night of the stay. */
    public int availableRooms(RoomType roomType, DateRange stay) {
        List<Booking> holding = bookingRepository.findOverlapping(
                roomType.getId(), stay.getCheckIn(), stay.getCheckOut(), BookingStatus.holdingRoom());

        int busiestNight = stay.nightDates()
                .mapToInt(night -> bookedOn(night, holding))
                .max()
                .orElse(0);
        return roomType.getTotalRooms() - busiestNight;
    }

    /** Rooms taken on a night: a booking can hold several rooms. */
    private int bookedOn(LocalDate night, List<Booking> bookings) {
        return bookings.stream()
                .filter(b -> b.getStay().includesNight(night))
                .mapToInt(Booking::getRooms)
                .sum();
    }
}
