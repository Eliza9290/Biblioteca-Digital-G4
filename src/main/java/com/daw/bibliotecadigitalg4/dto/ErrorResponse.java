package com.daw.bibliotecadigitalg4.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta estándar de error")

public class ErrorResponse {
    @Schema(description = "Código HTTP", example = "400")
    private int status;

    @Schema(description = "Mensaje descriptivo", example = "Error de validación")
    private String mensaje;

    @Schema(description = "Momento del error")
    private LocalDateTime timestamp;

    @Schema(description = "Detalle por campo (solo errores de validación)", example = "{\"emailSocio\": \"El email no tiene un formato válido\"}")
    private Map<String, String> erroresCampo;
}

