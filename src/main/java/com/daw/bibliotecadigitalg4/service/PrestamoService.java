package com.daw.bibliotecadigitalg4.service;

import com.daw.bibliotecadigitalg4.dto.PrestamoCreacionDTO;
import com.daw.bibliotecadigitalg4.dto.PrestamoPatchDTO;
import com.daw.bibliotecadigitalg4.dto.PrestamoResumenDTO;
import com.daw.bibliotecadigitalg4.exception.RecursoNoEncontradoException;
import com.daw.bibliotecadigitalg4.exception.ReglaNegocioException;
import com.daw.bibliotecadigitalg4.model.Prestamo;
import com.daw.bibliotecadigitalg4.repository.PrestamoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import com.daw.bibliotecadigitalg4.dto.LibroExternoDTO;
import com.daw.bibliotecadigitalg4.dto.PrestamoMashupDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrestamoService {

    public static final String CACHE_MASHUP = "prestamos-libro";
    public static final int MAX_PRESTAMOS_POR_SOCIO = 3;
    public static final String FUENTE_OPEN_LIBRARY = "Open Library API (tiempo real)";
    public static final String FUENTE_NO_DISPONIBLE = "Datos del libro no disponibles: Open Library no responde";
    private static final String URL_PORTADA = "https://covers.openlibrary.org/b/isbn/%s-M.jpg";

    private final PrestamoRepository prestamoRepository;
    private final ExternalApiService externalApiService;

    public List<PrestamoResumenDTO> listar(String titulo, String emailSocio) {
        List<Prestamo> prestamos;
        if (titulo != null && !titulo.isBlank()) {
            prestamos = prestamoRepository.findByTituloLibroContainingIgnoreCase(titulo);
        } else if (emailSocio != null && !emailSocio.isBlank()) {
            prestamos = prestamoRepository.findByEmailSocioIgnoreCase(emailSocio);
        } else {
            prestamos = prestamoRepository.findAll();
        }
        return prestamos.stream().map(this::aResumenDTO).toList();
    }

    public PrestamoResumenDTO buscarPorId(Long id) {
        return aResumenDTO(obtenerEntidad(id));
    }

    @Cacheable(value = CACHE_MASHUP, key = "#id", unless = "#result.tituloOficial == null")
    public PrestamoMashupDTO obtenerPrestamoConLibro(Long id) {
        log.info("--> [SERVICIO] Petición REAL a Open Library para el préstamo {}", id);
        Prestamo prestamo = obtenerEntidad(id);
        Optional<LibroExternoDTO> libro = externalApiService.obtenerDatosLibro(prestamo.getIsbn());

        return new PrestamoMashupDTO(
                prestamo.getId(),
                prestamo.getCodigo(),
                prestamo.getIsbn(),
                prestamo.getNombreSocio(),
                prestamo.getFechaDevolucion(),
                libro.map(LibroExternoDTO::getTitle).orElse(null),
                libro.map(LibroExternoDTO::getNumberOfPages).orElse(null),
                libro.map(LibroExternoDTO::getPublishDate).orElse(null),
                libro.map(this::primeraEditorial).orElse(null),
                libro.isPresent() ? String.format(URL_PORTADA, prestamo.getIsbn()) : null,
                libro.isPresent() ? FUENTE_OPEN_LIBRARY : FUENTE_NO_DISPONIBLE);
    }

    @CacheEvict(value = CACHE_MASHUP, allEntries = true)
    public PrestamoResumenDTO crear(PrestamoCreacionDTO dto) {
        validarLimitePrestamos(dto.getEmailSocio());
        Prestamo prestamo = new Prestamo();
        copiarDatos(dto, prestamo);
        return aResumenDTO(prestamoRepository.save(prestamo));
    }

    @CacheEvict(value = CACHE_MASHUP, allEntries = true)
    public PrestamoResumenDTO reemplazar(Long id, PrestamoCreacionDTO dto) {
        Prestamo prestamo = obtenerEntidad(id);
        if (cambiaDeSocio(prestamo, dto.getEmailSocio())) {
            validarLimitePrestamos(dto.getEmailSocio());
        }
        copiarDatos(dto, prestamo);
        return aResumenDTO(prestamoRepository.saveAndFlush(prestamo));
    }

    @CacheEvict(value = CACHE_MASHUP, allEntries = true)
    public PrestamoResumenDTO modificarParcial(Long id, PrestamoPatchDTO dto) {
        Prestamo prestamo = obtenerEntidad(id);
        if (dto.getEmailSocio() != null) {
            if (cambiaDeSocio(prestamo, dto.getEmailSocio())) {
                validarLimitePrestamos(dto.getEmailSocio());
            }
            prestamo.setEmailSocio(dto.getEmailSocio());
        }
        if (dto.getNombreSocio() != null) {
            prestamo.setNombreSocio(dto.getNombreSocio());
        }
        if (dto.getFianza() != null) {
            prestamo.setFianza(dto.getFianza());
        }
        if (dto.getFechaDevolucion() != null) {
            prestamo.setFechaDevolucion(dto.getFechaDevolucion());
        }
        if (dto.getGeneros() != null) {
            prestamo.getGeneros().clear();
            prestamo.getGeneros().addAll(dto.getGeneros());
        }
        return aResumenDTO(prestamoRepository.saveAndFlush(prestamo));
    }

    @CacheEvict(value = CACHE_MASHUP, allEntries = true)
    public void eliminar(Long id) {
        Prestamo prestamo = obtenerEntidad(id);
        prestamoRepository.delete(prestamo);
    }

    private Prestamo obtenerEntidad(Long id) {
        return prestamoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe ningún préstamo con id " + id));
    }

    private void validarLimitePrestamos(String emailSocio) {
        long prestamosActuales = prestamoRepository.countByEmailSocioIgnoreCase(emailSocio);
        if (prestamosActuales >= MAX_PRESTAMOS_POR_SOCIO) {
            throw new ReglaNegocioException("El socio " + emailSocio + " ya tiene " + prestamosActuales
                    + " préstamos. El máximo permitido es " + MAX_PRESTAMOS_POR_SOCIO);
        }
    }

    private boolean cambiaDeSocio(Prestamo prestamo, String nuevoEmail) {
        return !prestamo.getEmailSocio().equalsIgnoreCase(nuevoEmail);
    }

    private String primeraEditorial(LibroExternoDTO libro) {
        List<String> editoriales = libro.getPublishers();
        return (editoriales == null || editoriales.isEmpty()) ? null : editoriales.get(0);
    }

    private void copiarDatos(PrestamoCreacionDTO dto, Prestamo prestamo) {
        prestamo.setCodigo(dto.getCodigo());
        prestamo.setIsbn(dto.getIsbn());
        prestamo.setTituloLibro(dto.getTituloLibro());
        prestamo.setNombreSocio(dto.getNombreSocio());
        prestamo.setEmailSocio(dto.getEmailSocio());
        prestamo.setFianza(dto.getFianza());
        prestamo.setFechaPrestamo(dto.getFechaPrestamo());
        prestamo.setFechaDevolucion(dto.getFechaDevolucion());
        prestamo.setAceptaNormas(dto.getAceptaNormas());
        prestamo.getGeneros().clear();
        prestamo.getGeneros().addAll(dto.getGeneros());
    }

    private PrestamoResumenDTO aResumenDTO(Prestamo prestamo) {
        return new PrestamoResumenDTO(
                prestamo.getId(),
                prestamo.getCodigo(),
                prestamo.getIsbn(),
                prestamo.getTituloLibro(),
                prestamo.getNombreSocio(),
                prestamo.getFianza(),
                prestamo.getFechaDevolucion(),
                new ArrayList<>(prestamo.getGeneros()));
    }
}