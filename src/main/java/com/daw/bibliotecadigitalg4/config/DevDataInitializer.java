package com.daw.bibliotecadigitalg4.config;

import com.daw.bibliotecadigitalg4.model.Prestamo;
import com.daw.bibliotecadigitalg4.repository.PrestamoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("dev")
public class DevDataInitializer implements CommandLineRunner {

    private final PrestamoRepository prestamoRepository;

    public DevDataInitializer(PrestamoRepository prestamoRepository) {
        this.prestamoRepository = prestamoRepository;
    }

    @Override
    public void run(String... args) {

        if (prestamoRepository.count() > 0) {
            return;
        }

        prestamoRepository.saveAll(List.of(
                crear("PRE-001", "9780451524935", "1984", "Ana García",
                        "Ana@correo.com", 15.00,
                        List.of("novela", "distopía")),

                crear("PRE-002", "9780307474728", "Cien años de soledad", "Ana García",
                        "Ana@correo.com", 12.50,
                        List.of("novela", "realismo mágico")),

                crear("PRE-003", "9780061120084", "Matar a un ruiseñor", "Ana García",
                        "Ana@correo.com", 10.00,
                        List.of("novela", "clásico")),

                crear("PRE-004", "9780140328721", "Fantástico Sr. Fox", "Luis Pérez",
                        "luis@correo.com", 8.00,
                        List.of("infantil", "humor"))
        ));
    }

    private Prestamo crear(String codigo, String isbn, String titulo,
                           String socio, String email, Double fianza,
                           List<String> generos) {

        Prestamo prestamo = new Prestamo();

        prestamo.setCodigo(codigo);
        prestamo.setIsbn(isbn);
        prestamo.setTituloLibro(titulo);
        prestamo.setNombreSocio(socio);
        prestamo.setEmailSocio(email);
        prestamo.setFianza(fianza);
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setFechaDevolucion(LocalDate.now().plusDays(21));
        prestamo.setAceptaNormas(true);
        prestamo.getGeneros().addAll(generos);

        return prestamo;
    }

}
