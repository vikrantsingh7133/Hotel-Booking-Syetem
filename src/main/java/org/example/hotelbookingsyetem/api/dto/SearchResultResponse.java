package org.example.hotelbookingsyetem.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.hotelbookingsyetem.search.PropertySearchResult;
import org.example.hotelbookingsyetem.search.RoomOffer;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class SearchResultResponse {

    private final PropertyResponse property;
    private final List<RoomOfferResponse> availableRoomTypes;

    public static SearchResultResponse from(PropertySearchResult result) {
        return new SearchResultResponse(
                PropertyResponse.from(result.getProperty()),
                result.getOffers().stream().map(RoomOfferResponse::from).toList());
    }

    @Getter
    @AllArgsConstructor
    public static class RoomOfferResponse {

        private final String roomTypeId;
        private final String name;
        private final int maxGuests;
        private final int availableRooms;
        /** Rooms of this type needed for the guests (or as asked). */
        private final int roomsNeeded;
        private final BigDecimal avgPricePerNight;
        private final BigDecimal totalPrice;

        static RoomOfferResponse from(RoomOffer offer) {
            return new RoomOfferResponse(offer.getRoomType().getId(), offer.getRoomType().getName(),
                    offer.getRoomType().getMaxGuests(), offer.getAvailableRooms(), offer.getRoomsNeeded(),
                    offer.getAvgPricePerNight(), offer.getTotalPrice());
        }
    }
}
