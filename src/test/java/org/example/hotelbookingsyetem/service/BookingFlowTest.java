package org.example.hotelbookingsyetem.service;

import org.example.hotelbookingsyetem.model.Amenity;
import org.example.hotelbookingsyetem.model.Booking;
import org.example.hotelbookingsyetem.model.BookingStatus;
import org.example.hotelbookingsyetem.model.DateRange;
import org.example.hotelbookingsyetem.model.Location;
import org.example.hotelbookingsyetem.model.PaymentMethod;
import org.example.hotelbookingsyetem.model.PaymentStatus;
import org.example.hotelbookingsyetem.model.Property;
import org.example.hotelbookingsyetem.model.RoomType;
import org.example.hotelbookingsyetem.exception.ConflictException;
import org.example.hotelbookingsyetem.exception.ForbiddenException;
import org.example.hotelbookingsyetem.exception.InvalidRequestException;
import org.example.hotelbookingsyetem.exception.RoomNotAvailableException;
import org.example.hotelbookingsyetem.search.PropertySearchResult;
import org.example.hotelbookingsyetem.search.RoomOffer;
import org.example.hotelbookingsyetem.search.SearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** End-to-end business rules against the real H2 database. */
@SpringBootTest
class BookingFlowTest {

    private static final LocalDate CHECK_IN = LocalDate.now().plusDays(10);
    private static final DateRange STAY = new DateRange(CHECK_IN, CHECK_IN.plusDays(2));
    private static final Map<String, String> GOOD_UPI = Map.of("vpa", "guest@upi");
    private static final Map<String, String> BAD_UPI = Map.of("vpa", "fail@upi");

    @Autowired private UserService userService;
    @Autowired private OwnerService ownerService;
    @Autowired private PropertyService propertyService;
    @Autowired private BookingService bookingService;
    @Autowired private PaymentService paymentService;
    @Autowired private SearchService searchService;
    @Autowired private PriceService priceService;
    @Autowired private BookingDetailsService bookingDetailsService;

    private String city;
    private String userId;
    private Property property;
    private RoomType room;

    @BeforeEach
    void setUp() {
        city = "City-" + UUID.randomUUID();
        userId = newUser();
        String ownerId = ownerService.register("Taj Group", "taj@example.com").getId();

        Property draft = new Property(ownerId, "Taj Palace", new Location(city, "Colaba", "Apollo Bunder"),
                new BigDecimal("4.5"), Set.of(Amenity.WIFI, Amenity.POOL));
        draft.addRoomType(new RoomType("Deluxe", 2, 1, new BigDecimal("2000")));
        property = propertyService.addProperty(draft);
        room = property.getRoomTypes().get(0);
    }

    @Test
    void bookAndPayConfirmsBooking() {
        Booking booking = book(userId);
        PaymentOutcome outcome = paymentService.pay(booking.getId(), key(), PaymentMethod.UPI, GOOD_UPI);

        assertThat(outcome.getPayment().getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(outcome.getBooking().getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(booking.getAmount()).isEqualByComparingTo("4000");
    }

    @Test
    void lastRoomCannotBeDoubleBooked() {
        book(userId);

        assertThatThrownBy(() -> book(newUser())).isInstanceOf(RoomNotAvailableException.class);
    }

    @Test
    void nonOverlappingStayCanBookSameRoom() {
        book(userId);
        DateRange later = new DateRange(STAY.getCheckOut(), STAY.getCheckOut().plusDays(1));

        Booking next = bookingService.createBooking(newUser(), property.getId(), room.getId(), 1, 1, later);

        assertThat(next.getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
    }

    @Test
    void cancellingReleasesRoomAndRefunds() {
        Booking booking = book(userId);
        paymentService.pay(booking.getId(), key(), PaymentMethod.CARD, Map.of("cardNumber", "4111111111111111"));

        Booking cancelled = bookingService.cancel(booking.getId(), userId);

        assertThat(cancelled.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(cancelled.getRefundAmount()).isEqualByComparingTo("4000");
        assertThat(book(newUser()).getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
    }

    @Test
    void failedPaymentFailsBookingAndReleasesRoom() {
        Booking booking = book(userId);

        PaymentOutcome outcome = paymentService.pay(booking.getId(), key(), PaymentMethod.UPI, BAD_UPI);

        assertThat(outcome.getBooking().getStatus()).isEqualTo(BookingStatus.FAILED);
        assertThat(book(newUser()).getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
    }

    @Test
    void samePaymentKeyDoesNotChargeTwice() {
        Booking booking = book(userId);
        String key = key();

        PaymentOutcome first = paymentService.pay(booking.getId(), key, PaymentMethod.UPI, GOOD_UPI);
        PaymentOutcome retry = paymentService.pay(booking.getId(), key, PaymentMethod.UPI, GOOD_UPI);

        assertThat(retry.getPayment().getId()).isEqualTo(first.getPayment().getId());
    }

    @Test
    void paidBookingCannotBePaidAgainWithNewKey() {
        Booking booking = book(userId);
        paymentService.pay(booking.getId(), key(), PaymentMethod.UPI, GOOD_UPI);

        assertThatThrownBy(() -> paymentService.pay(booking.getId(), key(), PaymentMethod.UPI, GOOD_UPI))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void onlyBookingOwnerCanCancel() {
        Booking booking = book(userId);

        assertThatThrownBy(() -> bookingService.cancel(booking.getId(), newUser()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void bookingDetailsIncludePropertyAndRoomType() {
        Booking booking = book(userId);

        BookingDetails details = bookingDetailsService.get(booking.getId());

        assertThat(details.getProperty().getName()).isEqualTo("Taj Palace");
        assertThat(details.getRoomType().getName()).isEqualTo("Deluxe");
    }

    @Test
    void historyResolvesEachBookingToItsOwnProperty() {
        Property other = new Property(property.getOwnerId(), "Taj Lands End", new Location(city, "Bandra", null),
                new BigDecimal("4"), Set.of());
        other.addRoomType(new RoomType("Suite", 2, 1, new BigDecimal("3000")));
        other = propertyService.addProperty(other);
        RoomType suite = other.getRoomTypes().get(0);

        book(userId);
        bookingService.createBooking(userId, other.getId(), suite.getId(), 2, 1, STAY);

        assertThat(bookingDetailsService.historyForUser(userId, null, 0, 10).getContent())
                .extracting(d -> d.getProperty().getName() + "/" + d.getRoomType().getName())
                .containsExactlyInAnyOrder("Taj Palace/Deluxe", "Taj Lands End/Suite");
    }

    @Test
    void bookingHistoryIsPagedNewestFirst() {
        Booking first = bookOn(CHECK_IN);
        Booking second = bookOn(CHECK_IN.plusDays(5));
        Booking third = bookOn(CHECK_IN.plusDays(10));

        Page<BookingDetails> page0 = bookingDetailsService.historyForUser(userId, null, 0, 2);
        Page<BookingDetails> page1 = bookingDetailsService.historyForUser(userId, null, 1, 2);

        assertThat(page0.getTotalElements()).isEqualTo(3);
        assertThat(page0.getTotalPages()).isEqualTo(2);
        assertThat(page0.hasNext()).isTrue();
        assertThat(page0.getContent()).extracting(d -> d.getBooking().getId())
                .containsExactly(third.getId(), second.getId());
        assertThat(page1.getContent()).extracting(d -> d.getBooking().getId())
                .containsExactly(first.getId());
        assertThat(page1.hasNext()).isFalse();
    }

    @Test
    void bookingHistoryFiltersByStatus() {
        Booking cancelled = bookOn(CHECK_IN);
        bookOn(CHECK_IN.plusDays(5));
        bookingService.cancel(cancelled.getId(), userId);

        Page<BookingDetails> page = bookingDetailsService.historyForUser(userId, BookingStatus.CANCELLED, 0, 10);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getBooking().getId()).isEqualTo(cancelled.getId());
    }

    @Test
    void bookingHistoryRejectsBadPageSize() {
        assertThatThrownBy(() -> bookingDetailsService.historyForUser(userId, null, 0, 0))
                .isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> bookingDetailsService.historyForUser(userId, null, -1, 10))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void groupTooBigForOneRoomCanBookSeveralRooms() {
        RoomType family = addFamilyRooms(3);

        Booking booking = bookFamily(family, 5, 2, STAY);

        assertThat(booking.getRooms()).isEqualTo(2);
        assertThat(booking.getAmount()).isEqualByComparingTo("12000"); // 2 rooms x 2 nights x 3000
    }

    @Test
    void tooFewRoomsForGuestsIsRejected() {
        RoomType family = addFamilyRooms(3);

        assertThatThrownBy(() -> bookFamily(family, 5, 1, STAY))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("at least 2 rooms");
    }

    @Test
    void moreRoomsThanGuestsIsRejected() {
        RoomType family = addFamilyRooms(3);

        bookFamily(family, 3, 3, STAY);           // fully booked: input errors must still win over 409

        assertThatThrownBy(() -> bookFamily(family, 2, 3, STAY)).isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void availabilityCountsRoomsNotBookings() {
        RoomType family = addFamilyRooms(3);
        bookFamily(family, 5, 2, STAY);           // 1 room left

        assertThat(bookFamily(family, 2, 1, STAY).getRooms()).isEqualTo(1);
        assertThatThrownBy(() -> bookFamily(family, 1, 1, STAY)).isInstanceOf(RoomNotAvailableException.class);
    }

    @Test
    void searchShowsRoomsNeededAndPriceForAllRooms() {
        RoomType family = addFamilyRooms(3);

        RoomOffer offer = searchService.search(new SearchCriteria(city, null, STAY, 5, null, null, null, null, null))
                .stream().flatMap(r -> r.getOffers().stream())
                .filter(o -> o.getRoomType().getId().equals(family.getId()))
                .findFirst().orElseThrow();

        assertThat(offer.getRoomsNeeded()).isEqualTo(2);
        assertThat(offer.getAvgPricePerNight()).isEqualByComparingTo("3000");
        assertThat(offer.getTotalPrice()).isEqualByComparingTo("12000");
    }

    @Test
    void searchHidesRoomTypeWithoutEnoughFreeRooms() {
        RoomType family = addFamilyRooms(3);
        bookFamily(family, 2, 2, STAY);           // 1 room left, 5 guests need 2

        assertThat(searchService.search(new SearchCriteria(city, null, STAY, 5, null, null, null, null, null)))
                .flatExtracting(PropertySearchResult::getOffers)
                .noneMatch(o -> o.getRoomType().getId().equals(family.getId()));
    }

    @Test
    void unpaidBookingExpiresAndFreesTheRoom() {
        Booking booking = book(userId);

        bookingService.expireOverdue(LocalDateTime.now().plusMinutes(11));

        assertThat(bookingService.get(booking.getId()).getStatus()).isEqualTo(BookingStatus.EXPIRED);
        assertThat(book(newUser()).getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
    }

    @Test
    void bookingIsNotExpiredBeforeHoldTime() {
        Booking booking = book(userId);

        bookingService.expireOverdue(LocalDateTime.now().plusMinutes(9));

        assertThat(bookingService.get(booking.getId()).getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
    }

    @Test
    void expiryLeavesPaidBookingsAlone() {
        Booking booking = book(userId);
        paymentService.pay(booking.getId(), key(), PaymentMethod.UPI, GOOD_UPI);

        bookingService.expireOverdue(LocalDateTime.now().plusMinutes(11));

        assertThat(bookingService.get(booking.getId()).getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void expiredBookingCannotBePaid() {
        Booking booking = book(userId);
        bookingService.expireOverdue(LocalDateTime.now().plusMinutes(11));

        assertThatThrownBy(() -> paymentService.pay(booking.getId(), key(), PaymentMethod.UPI, GOOD_UPI))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void searchHidesFullyBookedRooms() {
        assertThat(search(null, null)).hasSize(1);

        book(userId);

        assertThat(search(null, null)).isEmpty();
    }

    @Test
    void searchAppliesFilters() {
        assertThat(search(Set.of(Amenity.WIFI), new BigDecimal("4.5"))).hasSize(1);
        assertThat(search(null, new BigDecimal("4.6"))).isEmpty();
        assertThat(search(Set.of(Amenity.GYM), null)).isEmpty();
    }

    @Test
    void bookingUsesDateSpecificPrice() {
        priceService.setPrice(room.getId(), CHECK_IN, CHECK_IN, new BigDecimal("5000"));

        assertThat(book(userId).getAmount()).isEqualByComparingTo("7000");
    }

    @Test
    void duplicateEmailIsRejected() {
        String email = "dup-" + UUID.randomUUID() + "@example.com";
        userService.register("A", email, null);

        assertThatThrownBy(() -> userService.register("B", email, null)).isInstanceOf(ConflictException.class);
    }

    @Test
    void concurrentBookingsForLastRoomOnlyOneWins() throws Exception {
        int attempts = 8;
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < attempts; i++) {
            String guest = newUser();
            tasks.add(() -> {
                try {
                    book(guest);
                    return true;
                } catch (RoomNotAvailableException e) {
                    return false;
                }
            });
        }

        ExecutorService pool = Executors.newFixedThreadPool(attempts);
        try {
            long wins = 0;
            for (Future<Boolean> result : pool.invokeAll(tasks)) {
                if (result.get()) {
                    wins++;
                }
            }
            assertThat(wins).isEqualTo(1);
        } finally {
            pool.shutdown();
        }
    }

    private Booking book(String guestId) {
        return bookingService.createBooking(guestId, property.getId(), room.getId(), 2, 1, STAY);
    }

    private RoomType addFamilyRooms(int totalRooms) {
        Property hotel = new Property(property.getOwnerId(), "Family Inn", new Location(city, "Juhu", null),
                new BigDecimal("3.7"), Set.of());
        hotel.addRoomType(new RoomType("Family", 3, totalRooms, new BigDecimal("3000")));
        return propertyService.addProperty(hotel).getRoomTypes().get(0);
    }

    private Booking bookFamily(RoomType family, int guests, int rooms, DateRange stay) {
        return bookingService.createBooking(newUser(), family.getPropertyId(), family.getId(), guests, rooms, stay);
    }

    private Booking bookOn(LocalDate checkIn) {
        return bookingService.createBooking(userId, property.getId(), room.getId(), 2, 1,
                new DateRange(checkIn, checkIn.plusDays(1)));
    }

    private List<PropertySearchResult> search(Set<Amenity> amenities, BigDecimal minRating) {
        return searchService.search(new SearchCriteria(city, null, STAY, 2, null, null, null, amenities, minRating));
    }

    private String newUser() {
        return userService.register("Guest", UUID.randomUUID() + "@example.com", null).getId();
    }

    private static String key() {
        return UUID.randomUUID().toString();
    }
}
