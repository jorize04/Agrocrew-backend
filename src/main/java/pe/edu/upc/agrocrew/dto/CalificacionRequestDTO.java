package pe.edu.upc.agrocrew.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Calificación de utilidad de una evaluación (US25).
 */
@Data
public class CalificacionRequestDTO {
    @Schema(example = "5", description = "De 1 (nada útil) a 5 (muy útil)")
    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación debe ser de 1 a 5")
    @Max(value = 5, message = "La calificación debe ser de 1 a 5")
    private Integer puntaje;

    @Schema(example = "Me ayudó a decidir qué sembrar en la parte baja.")
    @Size(max = 500, message = "El comentario no debe superar 500 caracteres")
    private String comentario;
}
