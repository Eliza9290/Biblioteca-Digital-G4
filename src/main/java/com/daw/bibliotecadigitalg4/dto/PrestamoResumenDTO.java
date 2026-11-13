package com.daw.bibliotecadigitalg4.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos de un préstamo que se devuelven al cliente")
public class PrestamoResumenDTO {

    @Schema(description = "Identificador", example = "1")
    private Long id;

    @Schema(description = "Código único", example = "PRE-001")
    private String codigo;

    @Schema(description = "ISBN del libro", example = "9780451524935")
    private String isbn;

    @Schema(description = "Título del libro", example = "1984")
    private String tituloLibro;

    @Schema(description = "Nombre del socio", example = "Ana García")
    private String nombreSocio;

    @Schema(description = "Fianza en euros", example = "15.50")
    private Double fianza;

    @Schema(description = "Fecha prevista de devolución", example = "2027-06-30")
    private LocalDate fechaDevolucion;

    @Schema(description = "Géneros del libro", example = "[\"novela\", \"distopía\"]")
    private List<String> generos;

}
