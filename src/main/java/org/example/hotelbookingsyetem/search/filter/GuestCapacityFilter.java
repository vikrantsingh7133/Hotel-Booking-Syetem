package org.example.hotelbookingsyetem.search.filter;

import org.example.hotelbookingsyetem.search.SearchCandidate;
import org.example.hotelbookingsyetem.search.SearchCriteria;
import org.springframework.stereotype.Component;

@Component
public class GuestCapacityFilter implements SearchFilter {

    @Override
    public boolean matches(SearchCandidate candidate, SearchCriteria criteria) {
        return candidate.getRoomType().canHost(criteria.getGuests(), candidate.getRooms());
    }
}
