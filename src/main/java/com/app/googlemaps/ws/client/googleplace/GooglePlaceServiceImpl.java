package com.app.googlemaps.ws.client.googleplace;

import com.app.googlemaps.api.ApiKeyInfo;
import com.app.googlemaps.api.NearBySearchResponse;
import com.app.googlemaps.api.PlaceDetail;
import com.app.googlemaps.config.AppConfigProperties;
import com.app.googlemaps.exception.MaxLimitException;
import com.app.googlemaps.utils.WebClientUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
public class GooglePlaceServiceImpl implements GooglePlaceService {
    private WebClient webClient;
    private AppConfigProperties appConfigProperties;
    private List<ApiKeyInfo> apiKeys;
    private static final Logger LOG = LoggerFactory.getLogger(GooglePlaceServiceImpl.class);

    @Autowired
    public GooglePlaceServiceImpl(@Qualifier("googleMapWebClient") WebClient webClient,
                                  AppConfigProperties appConfigProperties) {
        this.webClient = webClient;
        this.appConfigProperties = appConfigProperties;
    }

    @PostConstruct
    public void initializeKeys() {
        List<String> keys = appConfigProperties.getGoogleMapProperties().getKeys();
        if (CollectionUtils.isNotEmpty(keys)) {
            apiKeys = new CopyOnWriteArrayList<>();
            List<ApiKeyInfo> mappedKeys = keys.stream()
                    .map(key -> new ApiKeyInfo(key, "UNDER_LIMIT", LocalDateTime.now()))
                    .collect(Collectors.toList());
            apiKeys.addAll(mappedKeys);
        }
    }

    @Override
    public Mono<NearBySearchResponse> getNearByPlaces(String loc, String type
            , String radius) {
        String key = getUnderLimitApiKey();
        LOG.info("using key:{} for nearByPlaces", key);
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("location", loc);
        queryParams.put("type", type);
        queryParams.put("radius", radius);
        queryParams.put("key", key);
        return WebClientUtils
                .getForEntity(webClient, appConfigProperties.getGoogleMapProperties().getNearBySearchUri(),
                        queryParams, null)
                .flatMap(response -> response.bodyToMono(NearBySearchResponse.class))
                .flatMap(nearBySearchResponse -> {
                    String status = nearBySearchResponse.getStatus();
                    if ("OK".equalsIgnoreCase(status) || "ZERO_RESULTS".equalsIgnoreCase(status)) {
                        return Mono.just(nearBySearchResponse);
                    }
                    return handleNearBySearchResponse(status, key);
                });
    }

    @Override
    public Mono<PlaceDetail> getPlaceDetail(String placeId) {
        String key = getUnderLimitApiKey();
        LOG.info("using key:{} for placeDetails", key);
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("placeid", placeId);
        queryParams.put("fields", "formatted_address,id,name,rating,reviews,geometry");
        queryParams.put("key", key);
        return WebClientUtils
                .getForEntity(webClient, appConfigProperties.getGoogleMapProperties().getPlaceDetailsUri(),
                        queryParams, null)
                .flatMap(response -> response.bodyToMono(PlaceDetail.class))
                .flatMap(placeDetail -> {
                    String status = placeDetail.getStatus();
                    if ("OK".equalsIgnoreCase(status) || "ZERO_RESULTS".equalsIgnoreCase(status)) {
                        return Mono.just(placeDetail);
                    }
                    return handlePlaceDetailResponse(status, key);
                });
    }

    private String getUnderLimitApiKey() {
        Optional<ApiKeyInfo> key = apiKeys.stream()
                .filter(apiKeyInfo -> "UNDER_LIMIT".equalsIgnoreCase(apiKeyInfo.getStatus()))
                .findFirst();
        if (key.isPresent()) {
            //LOG.info("using key:{}", key.get().getKey());
            return key.get().getKey();
        }
        return null; // or throw error
    }

    private void setKeyStatus(String key, String status) {
        apiKeys.stream().forEach(apiKeyInfo -> {
            LOG.info("updating key:{} to status:{}", key, status);
            if (apiKeyInfo.getKey().equalsIgnoreCase(key)) {
                apiKeyInfo.setStatus(status);
                apiKeyInfo.setStatusTransmissionTime(LocalDateTime.now());
            }
        });
    }

    private Mono<? extends NearBySearchResponse> handleNearBySearchResponse(String status, String key) {
        if ("UNKNOWN_ERROR".equalsIgnoreCase(status)) {
            return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        } else if ("OVER_QUERY_LIMIT".equalsIgnoreCase(status)) {
            setKeyStatus(key, "OVER_LIMIT");
            return Mono.error(new MaxLimitException("query limit exceeded"));
        } else {
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND));
        }
    }

    private Mono<? extends PlaceDetail> handlePlaceDetailResponse(String status, String key) {
        if ("OVER_QUERY_LIMIT".equalsIgnoreCase(status)) {
            setKeyStatus(key, "OVER_LIMIT");
            return Mono.error(new MaxLimitException("query limit exceeded"));
        } else if ("UNKNOWN_ERROR".equalsIgnoreCase(status)) {
            return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        } else {
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND));
        }
    }
}
