package com.daw.bibliotecadigitalg4.controller;

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
    @DisplayName("POST válido devuelve 201 y el préstamo creado")
    void crearPrestamoValido() throws Exception {

        String json = objectMapper.writeValueAsString(prestamoValido());

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

        Prestamo prestamo = prestamoValido();
        prestamo.setCodigo("P10");
        prestamo.setEmailSocio("sin-arroba");

        String json = objectMapper.writeValueAsString(prestamo);

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

        prestamoRepository.save(
                prestamoDePrueba("PRE-010", "luis@correo.com")
        );

        String json = objectMapper.writeValueAsString(prestamoValido());

        mockMvc.perform(post("/api/prestamos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST para un socio que ya tiene 3 préstamos devuelve 422")
    void crearSuperaLimite() throws Exception {

        prestamoRepository.save(
                prestamoDePrueba("PRE-001", "marta@correo.com")
        );

        prestamoRepository.save(
                prestamoDePrueba("PRE-002", "marta@correo.com")
        );

        prestamoRepository.save(
                prestamoDePrueba("PRE-003", "marta@correo.com")
        );

        String json = objectMapper.writeValueAsString(prestamoValido());

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
    @DisplayName("DELETE de un préstamo existente devuelve 204 y lo borra de la BD")
    void eliminarPrestamo() throws Exception {

        Prestamo guardado = prestamoRepository.save(
                prestamoDePrueba("PRE-030", "luis@correo.com")
        );

        mockMvc.perform(delete("/api/prestamos/{id}", guardado.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/prestamos/{id}", guardado.getId()))
                .andExpect(status().isNotFound());
    }

    private Prestamo prestamoValido() {

        Prestamo prestamo = prestamoDePrueba(
                "PRE-010", "marta@correo.com"
        );

        prestamo.setIsbn("9780307474728");
        prestamo.setTituloLibro("Cien años de soledad");

        return prestamo;
    }

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
