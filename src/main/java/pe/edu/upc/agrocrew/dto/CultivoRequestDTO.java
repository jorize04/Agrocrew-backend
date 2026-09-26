package pe.edu.upc.agrocrew.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import pe.edu.upc.agrocrew.models.TipoCultivo;

import java.util.List;

@Data
public class CultivoRequestDTO {

    @Schema(example = "Papa")
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre no debe superar 60 caracteres")
    private String nombre;

    @Schema(example = "Solanum tuberosum")
    @Size(max = 100)
    private String nombreCientifico;

    @Schema(example = "TRANSITORIO")
    @NotNull(message = "El tipo es obligatorio")
    private TipoCultivo tipo;

    @Schema(example = "150", description = "Días del ciclo; dejar vacío en cultivos permanentes")
    @Positive(message = "El ciclo debe ser mayor a 0 días")
    private Integer cicloDias;

    @Schema(example = "Tubérculo base de la alimentación andina.")
    @Size(max = 500)
    private String descripcion;

    @Schema(example = "[\"A\"]", description = "Códigos de grupos CUM aptos: A, C, P, F o X")
    @NotEmpty(message = "Debe indicar al menos un grupo CUM apto")
    private List<String> gruposCum;

    @NotNull(message = "Los requerimientos son obligatorios")
    @Valid
    private RequerimientoCultivoDTO requerimiento;
}
