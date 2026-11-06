package org.example.hotelbookingsyetem.service;

import lombok.RequiredArgsConstructor;
import org.example.hotelbookingsyetem.model.Owner;
import org.example.hotelbookingsyetem.exception.NotFoundException;
import org.example.hotelbookingsyetem.repository.OwnerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private final OwnerRepository ownerRepository;

    @Transactional
    public Owner register(String name, String email) {
        return ownerRepository.save(new Owner(name, email));
    }

    public Owner get(String ownerId) {
        return ownerRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Owner", ownerId));
    }

    public void ensureExists(String ownerId) {
        if (!ownerRepository.existsById(ownerId)) {
            throw new NotFoundException("Owner", ownerId);
        }
    }
}
