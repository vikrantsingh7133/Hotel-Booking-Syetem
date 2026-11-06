package org.example.hotelbookingsyetem.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hotelbookingsyetem.api.dto.BookingResponse;
import org.example.hotelbookingsyetem.api.dto.CreateUserRequest;
import org.example.hotelbookingsyetem.api.dto.PageResponse;
import org.example.hotelbookingsyetem.api.dto.UserResponse;
import org.example.hotelbookingsyetem.model.BookingStatus;
import org.example.hotelbookingsyetem.service.BookingDetailsService;
import org.example.hotelbookingsyetem.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BookingDetailsService bookingDetailsService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody CreateUserRequest request) {
        return UserResponse.from(userService.register(request.getName(), request.getEmail(), request.getPhone()));
    }

    @GetMapping("/{userId}")
    public UserResponse get(@PathVariable String userId) {
        return UserResponse.from(userService.get(userId));
    }

    @GetMapping("/{userId}/booking-history")
    public PageResponse<BookingResponse> bookingHistory(@PathVariable String userId,
                                                        @RequestParam(required = false) BookingStatus status,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        return PageResponse.from(
                bookingDetailsService.historyForUser(userId, status, page, size), BookingResponse::from);
    }
}
