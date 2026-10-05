package pe.edu.upc.agrocrew.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Cuerpo opcional para evaluar un predio.
 */
@Data
public class EvaluacionRequestDTO {

    /** Cultivo específico a evaluar además del ranking; es opcional. */
    @Schema(description = "Opcional. Si se envía, además del ranking se evalúa ese cultivo en particular (US23). "
            + "Para una evaluación general envíe {}", nullable = true)
    private Long cultivoId;
}
