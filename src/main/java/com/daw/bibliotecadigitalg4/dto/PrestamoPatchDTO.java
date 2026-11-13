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
@Schema(description = "Modificación parcial de un préstamo: solo se cambian los campos enviados")
public class PrestamoPatchDTO {

    @Size(max = 100, message = "El nombre del socio no puede superar 100 caracteres")
    @Schema(description = "Nuevo nombre del socio", example = "Ana García López")
    private String nombreSocio;

    @Email(message = "El email no tiene un formato válido")
    @Schema(description = "Nuevo email del socio", example = "ana.garcia@correo.com")
    private String emailSocio;

    @Positive(message = "La fianza debe ser mayor que 0")
    @Digits(integer = 3, fraction = 2, message = "La fianza admite hasta 3 enteros y 2 decimales")
    @Schema(description = "Nueva fianza en euros", example = "20.00")
    private Double fianza;

    @Future(message = "La fecha de devolución debe ser futura")
    @Schema(description = "Nueva fecha de devolución (ampliar el préstamo)", example = "2027-07-15")
    private LocalDate fechaDevolucion;

    @Schema(description = "Nueva lista de géneros", example = "[\"novela\", \"clásico\"]")
    private List<String> generos;

}
