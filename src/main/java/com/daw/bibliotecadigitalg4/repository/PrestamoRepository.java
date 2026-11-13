package com.daw.bibliotecadigitalg4.repository;

import com.daw.bibliotecadigitalg4.model.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    Optional<Prestamo> findByCodigo(String codigo);

    List<Prestamo> findByEmailSocioIgnoreCase(String emailSocio);

    List<Prestamo> findByTituloLibroContainingIgnoreCase(String texto);

    long countByEmailSocioIgnoreCase(String emailSocio);

}
