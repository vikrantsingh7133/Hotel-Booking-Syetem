package org.example.hotelbookingsyetem.service;

import org.example.hotelbookingsyetem.exception.ConflictException;
import org.example.hotelbookingsyetem.model.Booking;
import org.example.hotelbookingsyetem.model.BookingStatus;
import org.example.hotelbookingsyetem.model.DateRange;
import org.example.hotelbookingsyetem.model.Location;
import org.example.hotelbookingsyetem.model.PaymentMethod;
import org.example.hotelbookingsyetem.model.Property;
import org.example.hotelbookingsyetem.model.RoomType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Hold time 0 = every booking is already past its deadline, so we can test
 * that pay() refuses it even before the expiry job has run.
 */
@SpringBootTest(properties = {
        "booking.hold-minutes=0",
        "spring.datasource.url=jdbc:h2:mem:expiredhold;DB_CLOSE_DELAY=-1"
})
class ExpiredHoldPaymentTest {

    @Autowired private UserService userService;
    @Autowired private OwnerService ownerService;
    @Autowired private PropertyService propertyService;
    @Autowired private BookingService bookingService;
    @Autowired private PaymentService paymentService;

    @Test
    void paymentAfterDeadlineIsRefusedEvenBeforeJobRuns() {
        String userId = userService.register("Guest", "late@example.com", null).getId();
        String ownerId = ownerService.register("Owner", "owner@example.com").getId();
        Property draft = new Property(ownerId, "Inn", new Location("Goa", null, null), new BigDecimal("4"), Set.of());
        draft.addRoomType(new RoomType("Std", 2, 1, new BigDecimal("1000")));
        Property property = propertyService.addProperty(draft);
        RoomType room = property.getRoomTypes().get(0);

        LocalDate checkIn = LocalDate.now().plusDays(5);
        Booking booking = bookingService.createBooking(userId, property.getId(), room.getId(), 1, 1,
                new DateRange(checkIn, checkIn.plusDays(1)));

        assertThatThrownBy(() -> paymentService.pay(booking.getId(), "k1", PaymentMethod.UPI, Map.of("vpa", "a@upi")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("hold expired");
        assertThat(bookingService.get(booking.getId()).getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
    }
}
