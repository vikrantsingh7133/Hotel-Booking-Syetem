package org.example.hotelbookingsyetem.service;

import lombok.RequiredArgsConstructor;
import org.example.hotelbookingsyetem.model.User;
import org.example.hotelbookingsyetem.exception.ConflictException;
import org.example.hotelbookingsyetem.exception.NotFoundException;
import org.example.hotelbookingsyetem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User register(String name, String email, String phone) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already registered: " + email);
        }
        return userRepository.save(new User(name, email, phone));
    }

    public User get(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));
    }

    public void ensureExists(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User", userId);
        }
    }
}
