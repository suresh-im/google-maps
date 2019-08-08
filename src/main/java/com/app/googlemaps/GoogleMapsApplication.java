package com.app.googlemaps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebFlux;

@EnableSwagger2WebFlux
@SpringBootApplication
public class GoogleMapsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoogleMapsApplication.class, args);
    }

}
