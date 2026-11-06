package org.example.hotelbookingsyetem.repository;

import jakarta.persistence.LockModeType;
import org.example.hotelbookingsyetem.model.Booking;
import org.example.hotelbookingsyetem.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.id = :id")
    Optional<Booking> findByIdForUpdate(@Param("id") String id);

    @Query("""
            select b from Booking b
            where b.roomTypeId = :roomTypeId
              and b.status in :statuses
              and b.checkIn < :checkOut
              and b.checkOut > :checkIn
            """)
    List<Booking> findOverlapping(@Param("roomTypeId") String roomTypeId,
                                  @Param("checkIn") LocalDate checkIn,
                                  @Param("checkOut") LocalDate checkOut,
                                  @Param("statuses") Collection<BookingStatus> statuses);

    /** Unpaid bookings whose hold time is over, locked so a payment can't confirm them at the same time. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.status = :status and b.expiresAt <= :now")
    List<Booking> findOverdueForUpdate(@Param("status") BookingStatus status, @Param("now") LocalDateTime now);

    Page<Booking> findByUserId(String userId, Pageable pageable);

    Page<Booking> findByUserIdAndStatus(String userId, BookingStatus status, Pageable pageable);
}
