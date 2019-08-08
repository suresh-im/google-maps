package com.app.googlemaps.service;

import com.app.googlemaps.api.PlaceDetail;
import com.app.googlemaps.exception.MaxLimitException;
import com.app.googlemaps.ws.client.googleplace.GooglePlaceService;
import com.app.googlemaps.ws.client.googleplace.GooglePlaceServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;

@Component
public class GoogleMapServiceImpl implements GoogleMapService {
    private GooglePlaceService googlePlaceService;
    private static final Logger LOG = LoggerFactory.getLogger(GoogleMapServiceImpl.class);

    @Autowired
    public GoogleMapServiceImpl(GooglePlaceService googlePlaceService) {
        this.googlePlaceService = googlePlaceService;
    }

    @Override
    public Flux<PlaceDetail.Result> getNearByPlaces(String lat, String lng,
                                                    String type, String radius) {
        return
                googlePlaceService.getNearByPlaces(StringUtils.joinWith(",", lat, lng),
                        type, radius)
                        .flatMapIterable(response -> response.getResults())
                        .parallel()
                        .runOn(Schedulers.parallel())
                        .flatMap(aPlace ->
                                googlePlaceService.getPlaceDetail(aPlace.getPlaceId())
                                        .onErrorMap(throwable -> {
                                            LOG.error("error:{}", throwable.getMessage());
                                            return throwable;
                                        })
                                        .retry(Long.valueOf("2"), throwable -> throwable instanceof MaxLimitException)
                                        .onErrorReturn(PlaceDetail.builder().result(PlaceDetail.Result.builder()
                                                .id(aPlace.getPlaceId())
                                                .geometry(aPlace.getGeometry())
                                                .rating(aPlace.getRating())
                                                .name(aPlace.getName())
                                                .build()).build())
                                        .filter(placeDetail -> Objects.nonNull(placeDetail.getResult()))
                                        .flatMap(placeDetail -> Mono.just(placeDetail.getResult()))
                        ).sequential();
    }

}
