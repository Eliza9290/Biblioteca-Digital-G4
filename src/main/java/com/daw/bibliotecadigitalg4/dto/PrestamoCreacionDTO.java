package com.daw.bibliotecadigitalg4.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos para crear o reemplazar un préstamo")
public class PrestamoCreacionDTO {

    @NotBlank(message = "El código es obligatorio")
    @Pattern(regexp = "^PRE-\\d{3}$", message = "El código debe tener el formato PRE-000 (ej. PRE-001)")
    @Schema(description = "Código único del préstamo", example = "PRE-010")
    private String codigo;

    @NotBlank(message = "El ISBN es obligatorio")
    @Pattern(regexp = "^(\\d{10}|\\d{13})$", message = "El ISBN debe tener 10 o 13 dígitos, sin guiones")
    @Schema(description = "ISBN del libro", example = "9780451524935")
    private String isbn;

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 2, max = 150, message = "El título debe tener entre 2 y 150 caracteres")
    @Schema(description = "Título del libro", example = "1984")
    private String tituloLibro;

    @NotBlank(message = "El nombre del socio es obligatorio")
    @Size(max = 100, message = "El nombre del socio no puede superar 100 caracteres")
    @Schema(description = "Nombre del socio", example = "Marta Ruiz")
    private String nombreSocio;

    @NotBlank(message = "El email del socio es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    @Schema(description = "Email del socio", example = "marta@correo.com")
    private String emailSocio;

    @NotNull(message = "La fianza es obligatoria")
    @Positive(message = "La fianza debe ser mayor que 0")
    @Digits(integer = 3, fraction = 2, message = "La fianza admite hasta 3 enteros y 2 decimales")
    @Schema(description = "Fianza en euros", example = "15.50")
    private Double fianza;

    @NotNull(message = "La fecha de préstamo es obligatoria")
    @PastOrPresent(message = "La fecha de préstamo no puede ser futura")
    @Schema(description = "Fecha del préstamo", example = "2026-11-01")
    private LocalDate fechaPrestamo;

    @NotNull(message = "La fecha de devolución es obligatoria")
    @Future(message = "La fecha de devolución debe ser futura")
    @Schema(description = "Fecha prevista de devolución", example = "2027-06-30")
    private LocalDate fechaDevolucion;

    @NotNull(message = "Debe indicar si acepta las normas")
    @AssertTrue(message = "El socio debe aceptar las normas de la biblioteca")
    @Schema(description = "Aceptación de las normas", example = "true")
    private Boolean aceptaNormas;

    @NotEmpty(message = "Debe indicar al menos un género")
    @Schema(description = "Géneros del libro", example = "[\"novela\", \"distopía\"]")
    private List<String> generos;

}
