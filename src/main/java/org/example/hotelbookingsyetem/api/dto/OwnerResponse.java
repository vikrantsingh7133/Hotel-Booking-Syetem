package org.example.hotelbookingsyetem.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.hotelbookingsyetem.model.Owner;

@Getter
@AllArgsConstructor
public class OwnerResponse {

    private final String id;
    private final String name;
    private final String email;

    public static OwnerResponse from(Owner owner) {
        return new OwnerResponse(owner.getId(), owner.getName(), owner.getEmail());
    }
}
