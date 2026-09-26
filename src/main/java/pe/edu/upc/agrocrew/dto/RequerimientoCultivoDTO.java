package pe.edu.upc.agrocrew.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import pe.edu.upc.agrocrew.models.ToleranciaInundacion;

/** Rangos que necesita el cultivo. Se usa tanto para registrar como para responder. */
@Data
public class RequerimientoCultivoDTO {

    @Schema(example = "2500")
    @NotNull(message = "La altitud mínima es obligatoria")
    @Min(value = 0, message = "La altitud mínima no puede ser negativa")
    @Max(value = 6800, message = "La altitud no puede superar 6800 msnm")
    private Integer altitudMin;

    @Schema(example = "4200")
    @NotNull(message = "La altitud máxima es obligatoria")
    @Min(value = 0, message = "La altitud máxima no puede ser negativa")
    @Max(value = 6800, message = "La altitud no puede superar 6800 msnm")
    private Integer altitudMax;

    @Schema(example = "8")
    @NotNull(message = "La temperatura mínima es obligatoria")
    @DecimalMin(value = "-20", message = "Temperatura fuera de rango")
    @DecimalMax(value = "50", message = "Temperatura fuera de rango")
    private Double temperaturaMin;

    @Schema(example = "20")
    @NotNull(message = "La temperatura máxima es obligatoria")
    @DecimalMin(value = "-20", message = "Temperatura fuera de rango")
    @DecimalMax(value = "50", message = "Temperatura fuera de rango")
    private Double temperaturaMax;

    @Schema(example = "500", description = "Lluvia anual mínima en mm")
    @NotNull(message = "La precipitación mínima es obligatoria")
    @PositiveOrZero(message = "La precipitación no puede ser negativa")
    private Double precipitacionMinMm;

    @Schema(example = "1200", description = "Lluvia anual máxima en mm")
    @NotNull(message = "La precipitación máxima es obligatoria")
    @PositiveOrZero(message = "La precipitación no puede ser negativa")
    private Double precipitacionMaxMm;

    @Schema(example = "BAJA")
    @NotNull(message = "La tolerancia a inundación es obligatoria")
    private ToleranciaInundacion toleranciaInundacion;

    @Schema(example = "false", description = "true si el cultivo no prospera solo con lluvia (secano)")
    @NotNull(message = "Debe indicar si requiere riego")
    private Boolean requiereRiego;
}
