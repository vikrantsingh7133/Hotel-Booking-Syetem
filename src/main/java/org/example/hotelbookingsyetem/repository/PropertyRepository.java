package org.example.hotelbookingsyetem.repository;

import org.example.hotelbookingsyetem.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, String> {

    List<Property> findByOwnerId(String ownerId);

    List<Property> findByLocationCityIgnoreCase(String city);
}
