package org.example.hotelbookingsyetem.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateOwnerRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;
}
