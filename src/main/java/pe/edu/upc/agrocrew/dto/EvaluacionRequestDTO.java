package pe.edu.upc.agrocrew.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EvaluacionRequestDTO {

    @Schema(description = "Opcional. Si se envía, además del ranking se evalúa ese cultivo en particular (US23). "
            + "Para una evaluación general envíe {}", nullable = true)
    private Long cultivoId;
}
