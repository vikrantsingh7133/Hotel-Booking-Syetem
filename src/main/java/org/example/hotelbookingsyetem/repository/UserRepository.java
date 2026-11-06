package org.example.hotelbookingsyetem.repository;

import org.example.hotelbookingsyetem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

    boolean existsByEmail(String email);
}
