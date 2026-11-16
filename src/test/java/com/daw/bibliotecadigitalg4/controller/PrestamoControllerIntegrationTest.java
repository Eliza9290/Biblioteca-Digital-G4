package com.daw.bibliotecadigitalg4.controller;

import com.daw.bibliotecadigitalg4.dto.PrestamoCreacionDTO;
import com.daw.bibliotecadigitalg4.dto.PrestamoPatchDTO;
import com.daw.bibliotecadigitalg4.model.Prestamo;
import com.daw.bibliotecadigitalg4.repository.PrestamoRepository;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Integración: HTTP → Controller → Service → Repository → H2")
public class PrestamoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @BeforeEach
    void limpiarBaseDeDatos() {
        prestamoRepository.deleteAll();
    }

    @Test
    @DisplayName("GET de la lista sin datos devuelve 200 y un array vacío")
    void listarVacio() throws Exception {

        mockMvc.perform(get("/api/prestamos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("POST válido devuelve 201 y el préstamo creado")
    void crearPrestamoValido() throws Exception {

        String json = objectMapper.writeValueAsString(
                prestamoCreacionDTO("PRE-010", "marta@correo.com")
        );

        mockMvc.perform(post("/api/prestamos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.codigo").value("PRE-010"))
                .andExpect(jsonPath("$.generos", hasSize(2)));
    }

    @Test
    @DisplayName("POST con datos no válidos devuelve 400 y los errores por campo")
    void crearPrestamoNoValido() throws Exception {

        PrestamoCreacionDTO dto = prestamoCreacionDTO("PRE-010", "marta@correo.com");
        dto.setCodigo("P10");
        dto.setEmailSocio("sin-arroba");

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/prestamos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erroresCampo.codigo").exists())
                .andExpect(jsonPath("$.erroresCampo.emailSocio").exists());
    }

    @Test
    @DisplayName("POST con código repetido devuelve 409")
    void crearPrestamoDuplicado() throws Exception {

        prestamoRepository.save(prestamoDePrueba("PRE-010", "luis@correo.com"));

        String json = objectMapper.writeValueAsString(
                prestamoCreacionDTO("PRE-010", "marta@correo.com")
        );

        mockMvc.perform(post("/api/prestamos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST para un socio que ya tiene 3 préstamos devuelve 422")
    void crearSuperaLimite() throws Exception {

        prestamoRepository.save(prestamoDePrueba("PRE-001", "marta@correo.com"));
        prestamoRepository.save(prestamoDePrueba("PRE-002", "marta@correo.com"));
        prestamoRepository.save(prestamoDePrueba("PRE-003", "marta@correo.com"));

        String json = objectMapper.writeValueAsString(
                prestamoCreacionDTO("PRE-010", "marta@correo.com")
        );

        mockMvc.perform(post("/api/prestamos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    @DisplayName("GET por id existente devuelve 200 y por id inexistente 404")
    void buscarPorId() throws Exception {

        Prestamo guardado = prestamoRepository.save(
                prestamoDePrueba("PRE-020", "luis@correo.com")
        );

        mockMvc.perform(get("/api/prestamos/{id}", guardado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("PRE-020"));

        mockMvc.perform(get("/api/prestamos/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT de un préstamo existente devuelve 200 y reemplaza sus datos")
    void reemplazarPrestamo() throws Exception {

        Prestamo guardado = prestamoRepository.save(
                prestamoDePrueba("PRE-040", "luis@correo.com")
        );

        PrestamoCreacionDTO dto = prestamoCreacionDTO("PRE-040", "luis@correo.com");
        dto.setTituloLibro("Cien años de soledad");

        mockMvc.perform(put("/api/prestamos/{id}", guardado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("PRE-040"));

        Prestamo enBd = prestamoRepository.findById(guardado.getId()).orElseThrow();
        assertThat(enBd.getTituloLibro()).isEqualTo("Cien años de soledad");
    }

    @Test
    @DisplayName("PATCH cambia solo el campo enviado y devuelve 200")
    void modificarParcialPrestamo() throws Exception {

        Prestamo guardado = prestamoRepository.save(
                prestamoDePrueba("PRE-050", "luis@correo.com")
        );

        PrestamoPatchDTO patch = new PrestamoPatchDTO();
        patch.setFianza(25.0);

        mockMvc.perform(patch("/api/prestamos/{id}", guardado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk());

        Prestamo enBd = prestamoRepository.findById(guardado.getId()).orElseThrow();
        assertThat(enBd.getFianza()).isEqualTo(25.0);
        assertThat(enBd.getTituloLibro()).isEqualTo("1984");
    }

    @Test
    @DisplayName("DELETE de un préstamo existente devuelve 204 vacío y lo borra de la BD")
    void eliminarPrestamo() throws Exception {

        Prestamo guardado = prestamoRepository.save(
                prestamoDePrueba("PRE-030", "luis@correo.com")
        );

        mockMvc.perform(delete("/api/prestamos/{id}", guardado.getId()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(get("/api/prestamos/{id}", guardado.getId()))
                .andExpect(status().isNotFound());
    }

    // DTO que el cliente envía a la API (lo que viaja por HTTP)
    private PrestamoCreacionDTO prestamoCreacionDTO(String codigo, String email) {

        PrestamoCreacionDTO dto = new PrestamoCreacionDTO();

        dto.setCodigo(codigo);
        dto.setIsbn("9780307474728");
        dto.setTituloLibro("Cien años de soledad");
        dto.setNombreSocio("Socio de prueba");
        dto.setEmailSocio(email);
        dto.setFianza(15.0);
        dto.setFechaPrestamo(LocalDate.now());
        dto.setFechaDevolucion(LocalDate.now().plusDays(21));
        dto.setAceptaNormas(true);
        dto.setGeneros(List.of("novela", "realismo mágico"));

        return dto;
    }

    // Entidad que se guarda directamente en la BD para preparar datos (no pasa por HTTP)
    private Prestamo prestamoDePrueba(String codigo, String email) {

        Prestamo prestamo = new Prestamo();

        prestamo.setCodigo(codigo);
        prestamo.setIsbn("9780451524935");
        prestamo.setTituloLibro("1984");
        prestamo.setNombreSocio("Socio de prueba");
        prestamo.setEmailSocio(email);
        prestamo.setFianza(15.0);
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setFechaDevolucion(LocalDate.now().plusDays(21));
        prestamo.setAceptaNormas(true);
        prestamo.getGeneros().addAll(List.of("novela", "distopía"));

        return prestamo;
    }
}