package org.example.hotelbookingsyetem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/** A special price for one room type on one date. Wins over the base price. */
@Entity
@Table(name = "room_price_overrides",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_type_id", "date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomPriceOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "room_type_id", nullable = false)
    private String roomTypeId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    public RoomPriceOverride(String roomTypeId, LocalDate date, BigDecimal price) {
        this.roomTypeId = roomTypeId;
        this.date = date;
        this.price = price;
    }

    public void changePrice(BigDecimal price) {
        this.price = price;
    }
}
