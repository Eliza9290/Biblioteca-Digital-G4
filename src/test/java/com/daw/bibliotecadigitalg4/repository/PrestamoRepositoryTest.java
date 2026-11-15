package com.daw.bibliotecadigitalg4.repository;

import com.daw.bibliotecadigitalg4.model.Prestamo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@DataJpaTest
@DisplayName("Pruebas JPA - PrestamoRepository")
public class PrestamoRepositoryTest {

    @Autowired
    private PrestamoRepository prestamoRepository;

    @BeforeEach
    void cargarDatos() {
        prestamoRepository.save(prestamo("PRE-001", "Cien años de soledad", "ana@correo.com"));
        prestamoRepository.save(prestamo("PRE-002", "El amor en los tiempos del cólera", "ana@correo.com"));
        prestamoRepository.save(prestamo("PRE-003", "1984", "luis@correo.com"));
    }

    @Test
    @DisplayName("findByEmailSocioIgnoreCase encuentra los préstamos de un socio sin mirar mayúsculas")
    void buscarPorSocio() {
        List<Prestamo> resultado = prestamoRepository.findByEmailSocioIgnoreCase("ANA@CORREO.COM");

        assertThat(resultado).hasSize(2)
                .extracting(Prestamo::getCodigo)
                .containsExactlyInAnyOrder("PRE-001", "PRE-002");
    }

    @Test
    @DisplayName("findByTituloLibroContainingIgnoreCase busca por parte del título")
    void buscarPorTitulo() {
        List<Prestamo> resultado = prestamoRepository.findByTituloLibroContainingIgnoreCase("SOLEDAD");

        assertThat(resultado).extracting(Prestamo::getCodigo).containsExactly("PRE-001");
    }

    @Test
    @DisplayName("countByEmailSocioIgnoreCase cuenta los préstamos de un socio")
    void contarPorSocio() {
        assertThat(prestamoRepository.countByEmailSocioIgnoreCase("ana@correo.com")).isEqualTo(2);
        assertThat(prestamoRepository.countByEmailSocioIgnoreCase("nadie@correo.com")).isZero();
    }

    @Test
    @DisplayName("La columna codigo es UNIQUE: guardar un código repetido falla")
    void codigoUnico() {
        assertThatThrownBy(() ->
                prestamoRepository.saveAndFlush(
                        prestamo("PRE-001", "Otro libro", "luis@correo.com")
                )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    //Metodo auxiliar para el @Before
    private Prestamo prestamo(String codigo, String titulo, String email) {
        Prestamo prestamo = new Prestamo();
        prestamo.setCodigo(codigo);
        prestamo.setIsbn("9780307474728");
        prestamo.setTituloLibro(titulo);
        prestamo.setNombreSocio("Socio de prueba");
        prestamo.setEmailSocio(email);
        prestamo.setFianza(10.0);
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setFechaDevolucion(LocalDate.now().plusDays(14));
        prestamo.setAceptaNormas(true);
        return prestamo;
    }
}
