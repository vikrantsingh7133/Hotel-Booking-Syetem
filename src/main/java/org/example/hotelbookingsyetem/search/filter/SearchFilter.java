package org.example.hotelbookingsyetem.search.filter;

import org.example.hotelbookingsyetem.search.SearchCandidate;
import org.example.hotelbookingsyetem.search.SearchCriteria;

/**
 * One search rule. New filter = new bean; SearchService picks it up automatically.
 * A filter must return true when its criterion is not set.
 */
public interface SearchFilter {

    boolean matches(SearchCandidate candidate, SearchCriteria criteria);
}
