package com.app.googlemaps.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceDetail implements Serializable {
    private Result result;
    private String status;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result {
        private NearBySearchResponse.Geometry geometry;
        private String id;
        private String name;
        private String rating;
        private List<Review> reviews;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Review {
        @JsonProperty("author_name")
        private String authorName;
        private String rating;
        private String text;
    }
}
