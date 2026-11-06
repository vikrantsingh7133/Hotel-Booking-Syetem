package org.example.hotelbookingsyetem.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hotelbookingsyetem.api.dto.CreateOwnerRequest;
import org.example.hotelbookingsyetem.api.dto.CreatePropertyRequest;
import org.example.hotelbookingsyetem.api.dto.OwnerResponse;
import org.example.hotelbookingsyetem.api.dto.PropertyResponse;
import org.example.hotelbookingsyetem.service.OwnerService;
import org.example.hotelbookingsyetem.service.PropertyService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/owners")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;
    private final PropertyService propertyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OwnerResponse register(@Valid @RequestBody CreateOwnerRequest request) {
        return OwnerResponse.from(ownerService.register(request.getName(), request.getEmail()));
    }

    @GetMapping("/{ownerId}")
    public OwnerResponse get(@PathVariable String ownerId) {
        return OwnerResponse.from(ownerService.get(ownerId));
    }

    @PostMapping("/{ownerId}/properties")
    @ResponseStatus(HttpStatus.CREATED)
    public PropertyResponse addProperty(@PathVariable String ownerId,
                                        @Valid @RequestBody CreatePropertyRequest request) {
        return PropertyResponse.from(propertyService.addProperty(request.toDomain(ownerId)));
    }

    @GetMapping("/{ownerId}/properties")
    public List<PropertyResponse> properties(@PathVariable String ownerId) {
        return propertyService.listByOwner(ownerId).stream().map(PropertyResponse::from).toList();
    }
}
