package org.example.hotelbookingsyetem.search.filter;

import org.example.hotelbookingsyetem.search.SearchCandidate;
import org.example.hotelbookingsyetem.search.SearchCriteria;
import org.springframework.stereotype.Component;

@Component
public class StarRatingFilter implements SearchFilter {

    @Override
    public boolean matches(SearchCandidate candidate, SearchCriteria criteria) {
        return criteria.getMinStarRating() == null
                || candidate.getProperty().getStarRating().compareTo(criteria.getMinStarRating()) >= 0;
    }
}
