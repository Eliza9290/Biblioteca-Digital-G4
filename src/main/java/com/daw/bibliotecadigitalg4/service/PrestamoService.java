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

@Service
@RequiredArgsConstructor
public class PrestamoService {

    public static final int MAX_PRESTAMOS_POR_SOCIO = 3;

    private final PrestamoRepository prestamoRepository;

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

    public PrestamoResumenDTO crear(PrestamoCreacionDTO dto) {
        validarLimitePrestamos(dto.getEmailSocio());
        Prestamo prestamo = new Prestamo();
        copiarDatos(dto, prestamo);
        return aResumenDTO(prestamoRepository.save(prestamo));
    }

    public PrestamoResumenDTO reemplazar(Long id, PrestamoCreacionDTO dto) {
        Prestamo prestamo = obtenerEntidad(id);
        if (cambiaDeSocio(prestamo, dto.getEmailSocio())) {
            validarLimitePrestamos(dto.getEmailSocio());
        }
        copiarDatos(dto, prestamo);
        return aResumenDTO(prestamoRepository.saveAndFlush(prestamo));
    }

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