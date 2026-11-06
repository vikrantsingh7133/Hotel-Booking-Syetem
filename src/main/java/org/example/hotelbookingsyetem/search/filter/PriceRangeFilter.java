package org.example.hotelbookingsyetem.search.filter;

import org.example.hotelbookingsyetem.search.SearchCandidate;
import org.example.hotelbookingsyetem.search.SearchCriteria;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Compares the average nightly price of the stay against the requested range. */
@Component
public class PriceRangeFilter implements SearchFilter {

    @Override
    public boolean matches(SearchCandidate candidate, SearchCriteria criteria) {
        BigDecimal price = candidate.getAvgPricePerNight();
        boolean aboveMin = criteria.getMinPrice() == null || price.compareTo(criteria.getMinPrice()) >= 0;
        boolean belowMax = criteria.getMaxPrice() == null || price.compareTo(criteria.getMaxPrice()) <= 0;
        return aboveMin && belowMax;
    }
}
