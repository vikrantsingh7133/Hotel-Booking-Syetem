package org.example.hotelbookingsyetem.api;

import lombok.RequiredArgsConstructor;
import org.example.hotelbookingsyetem.api.dto.PropertyResponse;
import org.example.hotelbookingsyetem.api.dto.SearchResultResponse;
import org.example.hotelbookingsyetem.model.Amenity;
import org.example.hotelbookingsyetem.model.DateRange;
import org.example.hotelbookingsyetem.search.SearchCriteria;
import org.example.hotelbookingsyetem.service.PropertyService;
import org.example.hotelbookingsyetem.service.SearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;
    private final SearchService searchService;

    @GetMapping("/search")
    public List<SearchResultResponse> search(@RequestParam String city,
                                             @RequestParam(required = false) String locality,
                                             @RequestParam LocalDate checkIn,
                                             @RequestParam LocalDate checkOut,
                                             @RequestParam(defaultValue = "1") int guests,
                                             @RequestParam(required = false) Integer rooms,
                                             @RequestParam(required = false) BigDecimal minPrice,
                                             @RequestParam(required = false) BigDecimal maxPrice,
                                             @RequestParam(required = false) Set<Amenity> amenities,
                                             @RequestParam(required = false) BigDecimal minRating) {
        SearchCriteria criteria = new SearchCriteria(city, locality, new DateRange(checkIn, checkOut),
                guests, rooms, minPrice, maxPrice, amenities, minRating);
        return searchService.search(criteria).stream().map(SearchResultResponse::from).toList();
    }

    @GetMapping("/{propertyId}")
    public PropertyResponse get(@PathVariable String propertyId) {
        return PropertyResponse.from(propertyService.get(propertyId));
    }
}
