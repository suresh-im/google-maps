package com.app.googlemaps.resource;


import com.app.googlemaps.api.PlaceDetail;
import com.app.googlemaps.exception.MaxLimitException;
import com.app.googlemaps.service.GoogleMapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class GoogleMapsResource {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleMapsResource.class);
    private GoogleMapService googleMapService;

    @Autowired
    public GoogleMapsResource(GoogleMapService googleMapService) {
        this.googleMapService = googleMapService;
    }

    @GetMapping(value = "/getNearByPlaces")
    public Flux<PlaceDetail.Result> getNearByPlaces(@RequestParam(value = "lat") String lat,
                                                    @RequestParam(value = "lng") String lng,
                                                    @RequestParam(value = "type") String type,
                                                    @RequestParam(value = "radius") String radius) {
        return googleMapService.getNearByPlaces(lat, lng, type, radius)
                .retry(Long.valueOf("2"), throwable -> throwable instanceof MaxLimitException)
                .onErrorMap(MaxLimitException.class, e -> new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "All Api keys exhausted"));
    }
}
