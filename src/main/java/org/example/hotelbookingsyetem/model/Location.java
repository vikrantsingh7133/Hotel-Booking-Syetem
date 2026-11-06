package org.example.hotelbookingsyetem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location {

    @Column(nullable = false)
    private String city;

    private String locality;

    private String address;

    public Location(String city, String locality, String address) {
        this.city = city;
        this.locality = locality;
        this.address = address;
    }
}
