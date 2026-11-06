package org.example.hotelbookingsyetem.exception;

public class RoomNotAvailableException extends ConflictException {

    public RoomNotAvailableException(String roomTypeId) {
        super("No rooms available for room type " + roomTypeId + " on the requested dates");
    }
}
