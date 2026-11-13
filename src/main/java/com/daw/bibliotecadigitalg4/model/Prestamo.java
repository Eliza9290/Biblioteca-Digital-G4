package com.daw.bibliotecadigitalg4.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prestamos")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Préstamo de un libro a un socio, guardado en la base de datos")
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador autogenerado", example = "1")
    private Long id;

    @Column(nullable = false, name = "codigo", unique = true, length = 10)
    @Schema(description = "Código único del préstamo", example = "PRE-001")
    private String codigo;

    @Column(nullable = false, name = "isbn", length = 13)
    @Schema(description = "ISBN del libro (10 o 13 dígitos)", example = "9780451524935")
    private String isbn;

    @Column(nullable = false, name = "titulo_libro", length = 150)
    @Schema(description = "Título del libro en nuestro catálogo", example = "1984")
    private String tituloLibro;

    @Column(nullable = false, name = "nombre_socio", length = 100)
    @Schema(description = "Nombre del socio que se lleva el libro", example = "Ana García")
    private String nombreSocio;

    @Column(nullable = false, name = "email_socio", length = 120)
    @Schema(description = "Email del socio", example = "ana@correo.com")
    private String emailSocio;

    @Column(nullable = false, name = "fianza")
    @Schema(description = "Fianza depositada en euros", example = "15.50")
    private Double fianza;

    @Column(nullable = false, name = "fecha_prestamo")
    @Schema(description = "Fecha en la que se prestó el libro", example = "2026-11-01")
    private LocalDate fechaPrestamo;

    @Column(nullable = false, name = "fecha_devolucion")
    @Schema(description = "Fecha prevista de devolución (futura)", example = "2027-06-30")
    private LocalDate fechaDevolucion;

    @Column(nullable = false, name = "acepta_normas")
    @Schema(description = "El socio acepta las normas de la biblioteca", example = "true")
    private Boolean aceptaNormas;

    @ElementCollection(fetch = FetchType.EAGER)  //Segunda tabla
    @CollectionTable(name = "prestamo_generos", joinColumns = @JoinColumn(name = "prestamo_id"))
    @Column(name = "genero", length = 60)
    @Schema(description = "Géneros del libro", example = "[\"novela\", \"distopía\"]") //["novela", "distopía"]
    private List<String> generos = new ArrayList<>();

}
