package org.example.hotelbookingsyetem.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.hotelbookingsyetem.model.Booking;
import org.example.hotelbookingsyetem.model.BookingStatus;
import org.example.hotelbookingsyetem.service.BookingDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BookingResponse {

    private final String id;
    private final String userId;
    private final PropertySummary property;
    private final RoomTypeSummary roomType;
    private final int guests;
    private final int rooms;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final BigDecimal amount;
    private final BigDecimal refundAmount;
    private final BookingStatus status;
    private final LocalDateTime expiresAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static BookingResponse from(BookingDetails details) {
        Booking b = details.getBooking();
        return new BookingResponse(b.getId(), b.getUserId(),
                PropertySummary.from(details.getProperty()), RoomTypeSummary.from(details.getRoomType()),
                b.getGuests(), b.getRooms(), b.getCheckIn(), b.getCheckOut(), b.getAmount(), b.getRefundAmount(),
                b.getStatus(), b.getExpiresAt(), b.getCreatedAt(), b.getUpdatedAt());
    }
}
