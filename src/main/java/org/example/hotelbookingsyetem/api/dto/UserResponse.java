package org.example.hotelbookingsyetem.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.hotelbookingsyetem.model.User;

@Getter
@AllArgsConstructor
public class UserResponse {

    private final String id;
    private final String name;
    private final String email;
    private final String phone;

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone());
    }
}
