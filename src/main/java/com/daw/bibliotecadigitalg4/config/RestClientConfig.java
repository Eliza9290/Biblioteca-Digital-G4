package com.daw.bibliotecadigitalg4.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableCaching

public class RestClientConfig {

    private static final String URL_OPEN_LIBRARY = "https://openlibrary.org";
    private static final Duration TIMEOUT_CONEXION = Duration.ofSeconds(3);
    private static final Duration TIMEOUT_LECTURA = Duration.ofSeconds(5);

    @Bean
    public RestClient restClient() {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(TIMEOUT_CONEXION);
        fabrica.setReadTimeout(TIMEOUT_LECTURA);

        return RestClient.builder()
                .baseUrl(URL_OPEN_LIBRARY)
                .requestFactory(fabrica)
                .build();
    }
}