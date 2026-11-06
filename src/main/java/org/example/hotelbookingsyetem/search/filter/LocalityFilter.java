package org.example.hotelbookingsyetem.search.filter;

import org.example.hotelbookingsyetem.search.SearchCandidate;
import org.example.hotelbookingsyetem.search.SearchCriteria;
import org.springframework.stereotype.Component;

@Component
public class LocalityFilter implements SearchFilter {

    @Override
    public boolean matches(SearchCandidate candidate, SearchCriteria criteria) {
        return criteria.getLocality() == null
                || criteria.getLocality().equalsIgnoreCase(candidate.getProperty().getLocation().getLocality());
    }
}
