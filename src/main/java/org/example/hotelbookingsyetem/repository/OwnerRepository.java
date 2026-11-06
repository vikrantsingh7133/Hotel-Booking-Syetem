package org.example.hotelbookingsyetem.repository;

import org.example.hotelbookingsyetem.model.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRepository extends JpaRepository<Owner, String> {
}
