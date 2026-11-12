package com.daw.bibliotecadigitalg4.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Préstamo local enriquecido con los datos reales del libro de Open Library")
public class PrestamoMashupDTO {

    @Schema(description = "Identificador (BD local)", example = "1")
    private Long id;

    @Schema(description = "Código del préstamo (BD local)", example = "PRE-001")
    private String codigo;

    @Schema(description = "ISBN (BD local)", example = "9780451524935")
    private String isbn;

    @Schema(description = "Socio que tiene el libro (BD local)", example = "Ana García")
    private String nombreSocio;

    @Schema(description = "Fecha prevista de devolución (BD local)", example = "2027-06-30")
    private LocalDate fechaDevolucion;

    @Schema(description = "Título oficial del libro (API externa)", example = "Nineteen Eighty-Four")
    private String tituloOficial;

    @Schema(description = "Número de páginas (API externa)", example = "328")
    private Integer numeroPaginas;

    @Schema(description = "Fecha de publicación de esta edición (API externa)", example = "1961")
    private String fechaPublicacion;

    @Schema(description = "Editorial (API externa)", example = "Signet Classics")
    private String editorial;

    @Schema(description = "URL de la portada (API externa)",
            example = "https://covers.openlibrary.org/b/isbn/9780451524935-M.jpg")
    private String portadaUrl;

    @Schema(description = "Origen de los datos del libro", example = "Open Library API (tiempo real)")
    private String fuenteDatos;
}