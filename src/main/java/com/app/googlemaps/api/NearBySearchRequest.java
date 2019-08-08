package com.app.googlemaps.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearBySearchRequest implements Serializable {
    private BigDecimal lat;
    private BigDecimal lng;
    private String type;
    private Integer radius;
}
