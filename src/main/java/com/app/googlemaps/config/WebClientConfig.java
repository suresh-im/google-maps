package com.app.googlemaps.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.SslProvider;
import reactor.netty.tcp.TcpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author avaka
 */
@Configuration
public class WebClientConfig {
    private ObjectMapper objectMapper;
    private AppConfig appConfig;
    private static final Logger LOGGER = LoggerFactory.getLogger(WebClientConfig.class);

    @Autowired
    public WebClientConfig(ObjectMapper objectMapper,
                           AppConfig appConfig) {
        this.objectMapper = objectMapper;
        this.appConfig = appConfig;
    }

    @Bean(name = "googleMapWebClient")
    public WebClient googleMapWebClient() {
        return getWebClient(appConfig.getAppConfigProperties().getGoogleMapProperties(), "google-map-service");
    }

    private WebClient getWebClient(WSProperties wsProperties, String client) {
        return WebClient.builder().baseUrl(wsProperties.getHost())
                .clientConnector(getReactorClientHttpConnector(getSslContext(), wsProperties, client))
                .exchangeStrategies(getExchangeStrategies())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(logRequest())
                .filter(logResponseStatus())
                .build();
    }

    private Integer getTimeOut(WSProperties wsProperties) {
        return wsProperties.getTimeout();
    }

    private SslContext getSslContext() {
        SslContext sslContext = null;
        try {
            sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } catch (Exception e) {
            LOGGER.warn("{}", "exception occurred while creating ssl-context", e);
        }
        return sslContext;
    }

    private ReactorClientHttpConnector getReactorClientHttpConnector(
            SslContext sslContext, WSProperties wsProperties, String client) {
        TcpClient tcpClient = TcpClient.create().secure(SslProvider.builder().sslContext(sslContext).build())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getTimeOut(wsProperties))
                .doOnConnected(connection ->
                        connection.addHandlerLast(new ReadTimeoutHandler(getTimeOut(wsProperties), TimeUnit.MILLISECONDS))
                );
        HttpClient httpClient = HttpClient.from(tcpClient);
        return new ReactorClientHttpConnector(httpClient);
    }

    private ExchangeStrategies getExchangeStrategies() {
        return ExchangeStrategies.builder()
                .codecs(clientCodecConfigurer -> {
                    clientCodecConfigurer.defaultCodecs()
                            .jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
                }).build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            List<String> headersList = new ArrayList<>();
            clientRequest.headers()
                    .forEach((name, values) -> values.forEach(value ->
                            headersList.add(new StringBuilder(name).append("=").append(value).toString())));
            LOGGER.info("#Request: #HttpMethod: {} #Url: {} #Headers: {}", clientRequest.method(), clientRequest.url(), headersList.toString());
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponseStatus() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            LOGGER.info("#Response: #HttpStatus {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }

}
