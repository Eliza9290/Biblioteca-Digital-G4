package com.daw.bibliotecadigitalg4.controller;

import com.daw.bibliotecadigitalg4.dto.ErrorResponse;
import com.daw.bibliotecadigitalg4.dto.PrestamoCreacionDTO;
import com.daw.bibliotecadigitalg4.dto.PrestamoMashupDTO;
import com.daw.bibliotecadigitalg4.dto.PrestamoPatchDTO;
import com.daw.bibliotecadigitalg4.dto.PrestamoResumenDTO;
import com.daw.bibliotecadigitalg4.service.PrestamoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
@RequiredArgsConstructor
@Tag(name = "Préstamos", description = "CRUD de préstamos de la biblioteca digital")
public class PrestamoController {
    private final PrestamoService prestamoService;

    @GetMapping
    @Operation(summary = "Listar préstamos",
            description = "Devuelve todos los préstamos. Se puede filtrar por parte del título o por el email del socio.")
    @ApiResponse(responseCode = "200", description = "Lista de préstamos (puede estar vacía)")
    public ResponseEntity<List<PrestamoResumenDTO>> listar(
            @Parameter(description = "Filtrar por parte del título (sin distinguir mayúsculas)", example = "soledad")
            @RequestParam(name = "titulo", required = false) String titulo,
            @Parameter(description = "Filtrar por email del socio", example = "ana@correo.com")
            @RequestParam(name = "email", required = false) String email) {
        return ResponseEntity.ok(prestamoService.listar(titulo, email));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar préstamo por id", description = "Devuelve un préstamo concreto.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Préstamo encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un préstamo con ese id",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PrestamoResumenDTO> buscarPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(prestamoService.buscarPorId(id));
    }
    @GetMapping("/{id}/libro")
    @Operation(summary = "Préstamo con los datos reales del libro (mashup)",
            description = "Junta los datos del préstamo guardados en H2 con la información del libro en Open Library "
                    + "(título oficial, páginas, editorial, portada). La primera llamada consulta la API; las siguientes "
                    + "salen de la caché RAM. Si Open Library no responde, devuelve el préstamo sin esos datos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Préstamo con los datos del libro (o con los datos por defecto)"),
            @ApiResponse(responseCode = "404", description = "No existe un préstamo con ese id",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PrestamoMashupDTO> obtenerConLibro(@PathVariable("id") Long id) {
        return ResponseEntity.ok(prestamoService.obtenerPrestamoConLibro(id));
    }

    @PostMapping
    @Operation(summary = "Crear préstamo", description = "Valida los datos y registra un préstamo nuevo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Préstamo creado"),
            @ApiResponse(responseCode = "400", description = "Datos no válidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe un préstamo con ese código",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "El socio ya tiene el máximo de 3 préstamos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PrestamoResumenDTO> crear(@Valid @RequestBody PrestamoCreacionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(prestamoService.crear(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Reemplazar préstamo", description = "Sustituye todos los datos de un préstamo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Préstamo reemplazado"),
            @ApiResponse(responseCode = "400", description = "Datos no válidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe un préstamo con ese id",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe otro préstamo con ese código",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "El nuevo socio ya tiene el máximo de 3 préstamos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PrestamoResumenDTO> reemplazar(@PathVariable("id") Long id,
                                                         @Valid @RequestBody PrestamoCreacionDTO dto) {
        return ResponseEntity.ok(prestamoService.reemplazar(id, dto));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Modificar parcialmente un préstamo",
            description = "Solo cambia los campos que se envían; los que no vienen se quedan igual. "
                    + "Por ejemplo, ampliar la fecha de devolución.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Préstamo modificado"),
            @ApiResponse(responseCode = "400", description = "Algún campo enviado no es válido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe un préstamo con ese id",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "El nuevo socio ya tiene el máximo de 3 préstamos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PrestamoResumenDTO> modificarParcial(@PathVariable("id") Long id,
                                                               @Valid @RequestBody PrestamoPatchDTO dto) {
        return ResponseEntity.ok(prestamoService.modificarParcial(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar préstamo", description = "Borra el préstamo (el libro se ha devuelto).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Préstamo eliminado (sin cuerpo)"),
            @ApiResponse(responseCode = "404", description = "No existe un préstamo con ese id",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        prestamoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
