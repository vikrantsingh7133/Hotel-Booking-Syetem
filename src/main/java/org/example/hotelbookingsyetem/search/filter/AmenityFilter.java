package org.example.hotelbookingsyetem.search.filter;

import org.example.hotelbookingsyetem.search.SearchCandidate;
import org.example.hotelbookingsyetem.search.SearchCriteria;
import org.springframework.stereotype.Component;

@Component
public class AmenityFilter implements SearchFilter {

    @Override
    public boolean matches(SearchCandidate candidate, SearchCriteria criteria) {
        return candidate.getProperty().hasAllAmenities(criteria.getAmenities());
    }
}
