package com.daw.bibliotecadigitalg4.controller;

import com.daw.bibliotecadigitalg4.dto.PrestamoResumenDTO;
import com.daw.bibliotecadigitalg4.exception.RecursoNoEncontradoException;
import com.daw.bibliotecadigitalg4.service.PrestamoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PrestamoController.class)
@ActiveProfiles("test")
@DisplayName("Unitario del controlador: capa web aislada con el servicio simulado")
class PrestamoControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PrestamoService prestamoService;

    private PrestamoResumenDTO resumen() {
        return new PrestamoResumenDTO(1L, "PRE-001", "9780451524935", "1984", "Ana García", 15.0,
                LocalDate.of(2027, 6, 30), List.of("novela"));
    }

    @Test
    @DisplayName("GET /api/prestamos devuelve 200 con la lista que da el servicio")
    void listar() throws Exception {
        when(prestamoService.listar(null, null)).thenReturn(List.of(resumen()));

        mockMvc.perform(get("/api/prestamos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].codigo").value("PRE-001"));

        verify(prestamoService).listar(null, null);
    }

    @Test
    @DisplayName("GET /api/prestamos/1 devuelve 200 con el préstamo")
    void buscarPorId() throws Exception {
        when(prestamoService.buscarPorId(1L)).thenReturn(resumen());

        mockMvc.perform(get("/api/prestamos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tituloLibro").value("1984"));

        verify(prestamoService).buscarPorId(1L);
    }

    @Test
    @DisplayName("GET de un id que no existe devuelve 404")
    void buscarPorIdInexistente() throws Exception {
        when(prestamoService.buscarPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("No existe ningún préstamo con id 99"));

        mockMvc.perform(get("/api/prestamos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe ningún préstamo con id 99"));
    }

    @Test
    @DisplayName("POST con JSON no válido devuelve 400 y el servicio no se llega a llamar")
    void crearNoValido() throws Exception {
        mockMvc.perform(post("/api/prestamos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\": \"mal\", \"isbn\": \"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erroresCampo.codigo").exists())
                .andExpect(jsonPath("$.erroresCampo.isbn").exists());

        verify(prestamoService, never()).crear(any());
    }
}

