package org.example.hotelbookingsyetem.service;

import lombok.RequiredArgsConstructor;
import org.example.hotelbookingsyetem.cancellation.CancellationPolicy;
import org.example.hotelbookingsyetem.model.Booking;
import org.example.hotelbookingsyetem.model.BookingStatus;
import org.example.hotelbookingsyetem.model.DateRange;
import org.example.hotelbookingsyetem.model.RoomType;
import org.example.hotelbookingsyetem.exception.ForbiddenException;
import org.example.hotelbookingsyetem.exception.InvalidRequestException;
import org.example.hotelbookingsyetem.exception.NotFoundException;
import org.example.hotelbookingsyetem.exception.RoomNotAvailableException;
import org.example.hotelbookingsyetem.pricing.PricingStrategy;
import org.example.hotelbookingsyetem.repository.BookingRepository;
import org.example.hotelbookingsyetem.repository.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    public static final int MAX_PAGE_SIZE = 100;

    /** Newest first; id breaks ties so pages stay stable. */
    private static final Sort NEWEST_FIRST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

    private final BookingRepository bookingRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final UserService userService;
    private final AvailabilityService availabilityService;
    private final PricingStrategy pricingStrategy;
    private final CancellationPolicy cancellationPolicy;
    private final Clock clock;

    @Value("${booking.hold-minutes}")
    private int holdMinutes;

    /**
     * Locks the room type row first, so two guests racing for the last room
     * are handled one after the other and only one wins.
     */
    @Transactional
    public Booking createBooking(String userId, String propertyId, String roomTypeId,
                                 int guests, int rooms, DateRange stay) {
        userService.ensureExists(userId);
        if (stay.getCheckIn().isBefore(LocalDate.now(clock))) {
            throw new InvalidRequestException("checkIn cannot be in the past");
        }

        RoomType roomType = roomTypeRepository.findByIdForUpdate(roomTypeId)
                .orElseThrow(() -> new NotFoundException("RoomType", roomTypeId));
        if (!roomType.belongsTo(propertyId)) {
            throw new InvalidRequestException("Room type " + roomTypeId + " does not belong to property " + propertyId);
        }
        if (!roomType.canHost(guests, rooms)) {
            throw new InvalidRequestException(guests + " guests need at least " + roomType.roomsNeededFor(guests)
                    + " rooms (max " + roomType.getMaxGuests() + " guests per room)");
        }
        BigDecimal amount = pricingStrategy.totalPrice(roomType, stay, rooms);
        Booking booking = new Booking(userId, propertyId, roomTypeId, guests, rooms, stay, amount,
                LocalDateTime.now(clock), Duration.ofMinutes(holdMinutes));

        // Checked last, after every input rule, so a 409 always means "really full".
        if (availabilityService.availableRooms(roomType, stay) < rooms) {
            throw new RoomNotAvailableException(roomTypeId);
        }
        return bookingRepository.save(booking);
    }

    /** Marks unpaid bookings past their hold time as EXPIRED, which frees their rooms. */
    @Transactional
    public int expireOverdue(LocalDateTime now) {
        List<Booking> overdue = bookingRepository.findOverdueForUpdate(BookingStatus.PENDING_PAYMENT, now);
        for (Booking booking : overdue) {
            booking.expire(now);
        }
        return overdue.size();
    }

    public Booking get(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking", bookingId));
    }

    /** One page of a user's bookings, newest first; {@code status} is optional. */
    public Page<Booking> historyForUser(String userId, BookingStatus status, int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidRequestException("page must be >= 0 and size between 1 and " + MAX_PAGE_SIZE);
        }
        userService.ensureExists(userId);
        PageRequest pageRequest = PageRequest.of(page, size, NEWEST_FIRST);
        return status == null
                ? bookingRepository.findByUserId(userId, pageRequest)
                : bookingRepository.findByUserIdAndStatus(userId, status, pageRequest);
    }

    @Transactional
    public Booking cancel(String bookingId, String userId) {
        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking", bookingId));
        if (!booking.isOwnedBy(userId)) {
            throw new ForbiddenException("Booking " + bookingId + " does not belong to user " + userId);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        if (!now.isBefore(booking.checkInTime())) {
            throw new InvalidRequestException("Booking cannot be cancelled after check-in time");
        }
        BigDecimal refund = booking.isPaid() ? cancellationPolicy.refundFor(booking, now) : BigDecimal.ZERO;
        booking.cancel(refund, now);
        return booking;
    }
}
