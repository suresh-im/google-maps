package com.app.googlemaps.utils;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;

/**
 * @author avaka
 */
public final class WebClientUtils {

    private WebClientUtils() {

    }

    private static final Logger LOG = LoggerFactory.getLogger(WebClientUtils.class);

    public static Mono<ClientResponse> getForEntity(WebClient webClient, String path,
                                                    Map<String, String> queryParams,
                                                    Map<String, ?> pathVariables) {
        return webClient.get()
                .uri(uriBuilder -> getUri(uriBuilder, path, queryParams, pathVariables))
                //.uri(uri, queryParams)
                .exchange()
                .flatMap(WebClientUtils::handleResponse);
//                .flatMap(response -> response.toEntity(responseType))
//                .onErrorMap(predicate -> true, throwable -> logError(path, throwable));
    }

    private static Mono<ClientResponse> handleResponse(ClientResponse response) {
        if (response.statusCode().isError()) {
            return response.body((clientHttpResponse, context) ->
                    throwWebClientResponseException(response)
            );
        }
        return Mono.just(response);
    }

    private static Mono<ClientResponse> throwWebClientResponseException(ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(errorBody -> {
                    LOG.error("client error body:{}", errorBody);
                    String msg = String.format("ClientResponse has erroneous status code: %d %s", response.statusCode().value(),
                            response.statusCode().getReasonPhrase());
                    WebClientResponseException webClientResponseException = new WebClientResponseException(msg,
                            response.statusCode().value(),
                            response.statusCode().getReasonPhrase(),
                            response.headers().asHttpHeaders(), errorBody.getBytes(), null);
                    return Mono.error(webClientResponseException);
                });
    }

    private static URI getUri(UriBuilder uriBuilder, String path, Map<String, String> queryParams,
                              Map<String, ?> pathVariables) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        if (MapUtils.isNotEmpty(queryParams)) {
            params.setAll(queryParams);
        }
        UriBuilder builder = uriBuilder
                .path(path)
                .queryParams(params);
        if (MapUtils.isNotEmpty(pathVariables)) {
            return builder.build(pathVariables);
        }
        return builder.build();
    }


}