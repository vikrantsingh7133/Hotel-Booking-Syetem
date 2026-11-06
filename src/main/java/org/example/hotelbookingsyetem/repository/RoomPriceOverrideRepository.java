package org.example.hotelbookingsyetem.repository;

import org.example.hotelbookingsyetem.model.RoomPriceOverride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RoomPriceOverrideRepository extends JpaRepository<RoomPriceOverride, String> {

    List<RoomPriceOverride> findByRoomTypeIdAndDateBetween(String roomTypeId, LocalDate from, LocalDate to);

    void deleteByRoomTypeIdAndDateBetween(String roomTypeId, LocalDate from, LocalDate to);
}
