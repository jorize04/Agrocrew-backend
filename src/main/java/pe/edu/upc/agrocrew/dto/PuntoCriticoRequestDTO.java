package pe.edu.upc.agrocrew.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import pe.edu.upc.agrocrew.models.NivelRiesgo;

/** Registro manual de un punto crítico (para pruebas o si el servicio de la ANA no está disponible). */
@Data
public class PuntoCriticoRequestDTO {

    @Schema(example = "MANUAL-001")
    @NotBlank(message = "El código es obligatorio")
    @Size(max = 60)
    private String codigo;

    @Schema(example = "Desborde del río Mantaro en el sector Pilcomayo")
    @Size(max = 500)
    private String descripcion;

    @Schema(example = "DESBORDE")
    @Size(max = 100)
    private String tipoPeligro;

    @Schema(example = "ALTO")
    @NotNull(message = "El nivel de riesgo es obligatorio")
    private NivelRiesgo nivelRiesgo;

    @Schema(example = "-12.0600")
    @NotNull
    @DecimalMin(value = "-18.4", message = "La ubicación debe estar dentro del Perú")
    @DecimalMax(value = "0.1", message = "La ubicación debe estar dentro del Perú")
    private Double latitud;

    @Schema(example = "-75.2200")
    @NotNull
    @DecimalMin(value = "-81.4", message = "La ubicación debe estar dentro del Perú")
    @DecimalMax(value = "-68.6", message = "La ubicación debe estar dentro del Perú")
    private Double longitud;
}
