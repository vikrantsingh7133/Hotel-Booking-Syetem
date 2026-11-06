package org.example.hotelbookingsyetem.domain;

import org.example.hotelbookingsyetem.exception.InvalidRequestException;
import org.example.hotelbookingsyetem.model.Location;
import org.example.hotelbookingsyetem.model.Property;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PropertyTest {

    private Property withRating(String rating) {
        return new Property("owner", "Hotel", new Location("Goa", null, null), new BigDecimal(rating), Set.of());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "3.7", "4.5", "5.0", "4.50"})
    void acceptsRatingsWithOneDecimal(String rating) {
        assertThat(withRating(rating).getStarRating()).isEqualByComparingTo(rating);
    }

    @Test
    void storesRatingWithOneDecimalPlace() {
        assertThat(withRating("4").getStarRating()).hasToString("4.0");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.9", "5.1", "4.55"})
    void rejectsOutOfRangeOrTooPreciseRatings(String rating) {
        assertThatThrownBy(() -> withRating(rating)).isInstanceOf(InvalidRequestException.class);
    }
}
