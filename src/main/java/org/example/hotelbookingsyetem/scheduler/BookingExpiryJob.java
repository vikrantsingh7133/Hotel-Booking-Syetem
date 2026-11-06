package org.example.hotelbookingsyetem.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hotelbookingsyetem.service.BookingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

/** Every few seconds, expires unpaid bookings whose hold time is over. */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingExpiryJob {

    private final BookingService bookingService;
    private final Clock clock;

    @Scheduled(fixedDelayString = "${booking.expiry-job-interval-ms}")
    public void run() {
        int expired = bookingService.expireOverdue(LocalDateTime.now(clock));
        if (expired > 0) {
            log.info("Expired {} unpaid booking(s)", expired);
        }
    }
}
