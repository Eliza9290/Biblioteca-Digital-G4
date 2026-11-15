package com.daw.bibliotecadigitalg4.service;

import com.daw.bibliotecadigitalg4.dto.PrestamoCreacionDTO;
import com.daw.bibliotecadigitalg4.dto.PrestamoResumenDTO;
import com.daw.bibliotecadigitalg4.exception.RecursoNoEncontradoException;
import com.daw.bibliotecadigitalg4.exception.ReglaNegocioException;
import com.daw.bibliotecadigitalg4.model.Prestamo;
import com.daw.bibliotecadigitalg4.repository.PrestamoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unitario del servicio: lógica de negocio con el repositorio simulado")
class PrestamoServiceUnitTest {

    @Mock
    private PrestamoRepository prestamoRepository;

    @InjectMocks
    private PrestamoService prestamoService;

    private Prestamo prestamo() {
        Prestamo prestamo = new Prestamo();
        prestamo.setId(1L);
        prestamo.setCodigo("PRE-001");
        prestamo.setIsbn("9780451524935");
        prestamo.setTituloLibro("1984");
        prestamo.setNombreSocio("Ana García");
        prestamo.setEmailSocio("ana@correo.com");
        prestamo.setFianza(15.0);
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setFechaDevolucion(LocalDate.now().plusDays(21));
        prestamo.setAceptaNormas(true);
        prestamo.getGeneros().add("novela");
        return prestamo;
    }

    private PrestamoCreacionDTO creacionDTO(String email) {
        return new PrestamoCreacionDTO("PRE-010", "9780307474728", "Cien años de soledad", "Marta Ruiz", email,
                12.5, LocalDate.now(), LocalDate.now().plusDays(14), true, List.of("novela"));
    }

    @Test
    @DisplayName("buscarPorId devuelve el DTO cuando el préstamo existe")
    void buscarPorIdExistente() {
        when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo()));

        PrestamoResumenDTO resultado = prestamoService.buscarPorId(1L);

        assertThat(resultado.getCodigo()).isEqualTo("PRE-001");
        assertThat(resultado.getGeneros()).containsExactly("novela");
    }

    @Test
    @DisplayName("buscarPorId lanza RecursoNoEncontradoException si no existe")
    void buscarPorIdInexistente() {
        when(prestamoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> prestamoService.buscarPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("crear guarda el préstamo si el socio tiene menos de 3 préstamos")
    void crearValido() {
        when(prestamoRepository.countByEmailSocioIgnoreCase("marta@correo.com")).thenReturn(1L);
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(invocacion -> {
            Prestamo guardado = invocacion.getArgument(0);
            guardado.setId(5L);
            return guardado;
        });

        PrestamoResumenDTO resultado = prestamoService.crear(creacionDTO("marta@correo.com"));

        assertThat(resultado.getId()).isEqualTo(5L);
        assertThat(resultado.getTituloLibro()).isEqualTo("Cien años de soledad");
        verify(prestamoRepository).save(any(Prestamo.class));
    }

    @Test
    @DisplayName("crear lanza ReglaNegocioException si el socio ya tiene 3 préstamos y no guarda nada")
    void crearSuperaLimite() {
        when(prestamoRepository.countByEmailSocioIgnoreCase("ana@correo.com")).thenReturn(3L);

        assertThatThrownBy(() -> prestamoService.crear(creacionDTO("ana@correo.com")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("máximo permitido es 3");

        verify(prestamoRepository, never()).save(any());
    }

    @Test
    @DisplayName("eliminar un id inexistente lanza 404 y nunca llama a delete")
    void eliminarInexistente() {
        when(prestamoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> prestamoService.eliminar(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(prestamoRepository, never()).delete(any());
    }
}