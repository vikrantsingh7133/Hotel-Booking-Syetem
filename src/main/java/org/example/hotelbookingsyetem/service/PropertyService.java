package org.example.hotelbookingsyetem.service;

import lombok.RequiredArgsConstructor;
import org.example.hotelbookingsyetem.model.Property;
import org.example.hotelbookingsyetem.exception.NotFoundException;
import org.example.hotelbookingsyetem.repository.PropertyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final OwnerService ownerService;

    /** Same path for a standalone hotel and a chain: both are owners adding properties. */
    @Transactional
    public Property addProperty(Property property) {
        ownerService.ensureExists(property.getOwnerId());
        return propertyRepository.save(property);
    }

    public Property get(String propertyId) {
        return propertyRepository.findById(propertyId)
                .orElseThrow(() -> new NotFoundException("Property", propertyId));
    }

    public List<Property> listByOwner(String ownerId) {
        ownerService.ensureExists(ownerId);
        return propertyRepository.findByOwnerId(ownerId);
    }
}
