package org.example.hotelbookingsyetem.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hotelbookingsyetem.api.dto.BookingResponse;
import org.example.hotelbookingsyetem.api.dto.CancelBookingRequest;
import org.example.hotelbookingsyetem.api.dto.CreateBookingRequest;
import org.example.hotelbookingsyetem.api.dto.PayRequest;
import org.example.hotelbookingsyetem.api.dto.PaymentResponse;
import org.example.hotelbookingsyetem.model.Booking;
import org.example.hotelbookingsyetem.model.DateRange;
import org.example.hotelbookingsyetem.service.BookingDetailsService;
import org.example.hotelbookingsyetem.service.BookingService;
import org.example.hotelbookingsyetem.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final BookingDetailsService bookingDetailsService;
    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@Valid @RequestBody CreateBookingRequest request) {
        DateRange stay = new DateRange(request.getCheckIn(), request.getCheckOut());
        Booking booking = bookingService.createBooking(
                request.getUserId(), request.getPropertyId(), request.getRoomTypeId(),
                request.getGuests(), request.getRooms(), stay);
        return BookingResponse.from(bookingDetailsService.toDetails(booking));
    }

    @GetMapping("/{bookingId}")
    public BookingResponse get(@PathVariable String bookingId) {
        return BookingResponse.from(bookingDetailsService.get(bookingId));
    }

    @PostMapping("/{bookingId}/payments")
    public PaymentResponse pay(@PathVariable String bookingId,
                               @Valid @RequestBody PayRequest request) {
        return PaymentResponse.from(
                paymentService.pay(bookingId, request.getIdempotencyKey(), request.getMethod(), request.getDetails()));
    }

    @PostMapping("/{bookingId}/cancel")
    public BookingResponse cancel(@PathVariable String bookingId,
                                  @Valid @RequestBody CancelBookingRequest request) {
        Booking booking = bookingService.cancel(bookingId, request.getUserId());
        return BookingResponse.from(bookingDetailsService.toDetails(booking));
    }
}
