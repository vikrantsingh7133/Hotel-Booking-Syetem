package org.example.hotelbookingsyetem.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.hotelbookingsyetem.model.Booking;
import org.example.hotelbookingsyetem.model.Property;
import org.example.hotelbookingsyetem.model.RoomType;

/** A booking together with the property and room type it refers to. */
@Getter
@AllArgsConstructor
public class BookingDetails {

    private final Booking booking;
    private final Property property;
    private final RoomType roomType;
}
