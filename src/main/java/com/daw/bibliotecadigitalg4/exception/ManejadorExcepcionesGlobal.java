package com.daw.bibliotecadigitalg4.exception;
import com.daw.bibliotecadigitalg4.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
@Slf4j
public class ManejadorExcepcionesGlobal {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));
        return construir(HttpStatus.BAD_REQUEST, "Error de validación en los datos enviados", errores);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarJsonIlegible(HttpMessageNotReadableException ex) {
        return construir(HttpStatus.BAD_REQUEST,
                "El cuerpo de la petición no es un JSON válido o tiene un formato incorrecto (las fechas van como 2027-06-30)",
                null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> manejarTipoIncorrecto(MethodArgumentTypeMismatchException ex) {
        return construir(HttpStatus.BAD_REQUEST,
                "El parámetro '" + ex.getName() + "' tiene un valor no válido: " + ex.getValue(), null);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> manejarRutaInexistente(NoResourceFoundException ex) {
        return construir(HttpStatus.NOT_FOUND, "La ruta solicitada no existe: /" + ex.getResourcePath(), null);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> manejarReglaNegocio(ReglaNegocioException ex) {
        return construir(HttpStatus.valueOf(422), ex.getMessage(), null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> manejarIntegridad(DataIntegrityViolationException ex) {
        return construir(HttpStatus.CONFLICT,
                "Conflicto con los datos guardados: ya existe un préstamo con ese código", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarGenerico(Exception ex) {
        log.error("--> [ERROR 500] Error no controlado", ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor. Inténtelo de nuevo más tarde", null);
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus estado, String mensaje, Map<String, String> errores) {
        ErrorResponse cuerpo = new ErrorResponse(estado.value(), mensaje, LocalDateTime.now(), errores);
        return ResponseEntity.status(estado).body(cuerpo);
    }

}
