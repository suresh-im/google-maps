package com.app.googlemaps.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyInfo {
    private String key;
    private String status;
    private LocalDateTime statusTransmissionTime;
}
