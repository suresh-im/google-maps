package com.app.googlemaps.config;

import lombok.Data;

@Data
public class WSProperties {
    private String host;
    private Integer timeout;
    private RetryableProperties retryableProperties;

    @Data
    public static class RetryableProperties {
        private Integer numRetries;
        private Integer timeout;
    }

}

