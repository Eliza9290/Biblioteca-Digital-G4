package com.daw.bibliotecadigitalg4.service;

import com.daw.bibliotecadigitalg4.dto.LibroExternoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalApiService {

    private final RestClient restClient;

    public Optional<LibroExternoDTO> obtenerDatosLibro(String isbn) {
        try {
            LibroExternoDTO respuesta = restClient.get()
                    .uri("/isbn/{isbn}.json", isbn)
                    .retrieve()
                    .body(LibroExternoDTO.class);

            if (respuesta == null || respuesta.getTitle() == null) {
                return Optional.empty();
            }
            return Optional.of(respuesta);
        } catch (RestClientException ex) {
            log.warn("--> [FALLBACK] Open Library no devuelve datos del ISBN {}: {}", isbn, ex.getMessage());
            return Optional.empty();
        }
    }
}

