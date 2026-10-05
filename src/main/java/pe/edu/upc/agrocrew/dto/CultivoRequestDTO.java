package pe.edu.upc.agrocrew.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import pe.edu.upc.agrocrew.models.TipoCultivo;

import java.util.List;

/**
 * Datos para crear o editar un cultivo del catálogo.
 */
@Data
public class CultivoRequestDTO {

    /** Nombre común del cultivo (máximo 60 caracteres). */
    @Schema(example = "Papa")
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre no debe superar 60 caracteres")
    private String nombre;

    /** Nombre científico, opcional (máximo 100 caracteres). */
    @Schema(example = "Solanum tuberosum")
    @Size(max = 100)
    private String nombreCientifico;

    /** Tipo de cultivo. */
    @Schema(example = "TRANSITORIO")
    @NotNull(message = "El tipo es obligatorio")
    private TipoCultivo tipo;

    /** Días del ciclo; se deja vacío en cultivos permanentes. */
    @Schema(example = "150", description = "Días del ciclo; dejar vacío en cultivos permanentes")
    @Positive(message = "El ciclo debe ser mayor a 0 días")
    private Integer cicloDias;

    /** Descripción breve (máximo 500 caracteres). */
    @Schema(example = "Tubérculo base de la alimentación andina.")
    @Size(max = 500)
    private String descripcion;

    /** Códigos de los grupos CUM aptos (A, C, P, F o X); se exige al menos uno. */
    @Schema(example = "[\"A\"]", description = "Códigos de grupos CUM aptos: A, C, P, F o X")
    @NotEmpty(message = "Debe indicar al menos un grupo CUM apto")
    private List<String> gruposCum;

    /** Requerimientos del cultivo. */
    @NotNull(message = "Los requerimientos son obligatorios")
    @Valid
    private RequerimientoCultivoDTO requerimiento;
}
