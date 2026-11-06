package org.example.hotelbookingsyetem.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hotelbookingsyetem.api.dto.DailyPriceResponse;
import org.example.hotelbookingsyetem.api.dto.SetPriceRequest;
import org.example.hotelbookingsyetem.service.PriceService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/room-types/{roomTypeId}/prices")
@RequiredArgsConstructor
public class PriceController {

    private final PriceService priceService;

    @PutMapping
    public List<DailyPriceResponse> setPrice(@PathVariable String roomTypeId,
                                             @Valid @RequestBody SetPriceRequest request) {
        return DailyPriceResponse.from(
                priceService.setPrice(roomTypeId, request.getFrom(), request.getTo(), request.getPrice()));
    }

    @GetMapping
    public List<DailyPriceResponse> dailyPrices(@PathVariable String roomTypeId,
                                                @RequestParam LocalDate from,
                                                @RequestParam LocalDate to) {
        return DailyPriceResponse.from(priceService.dailyPrices(roomTypeId, from, to));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePrices(@PathVariable String roomTypeId,
                             @RequestParam LocalDate from,
                             @RequestParam LocalDate to) {
        priceService.removePrices(roomTypeId, from, to);
    }
}
