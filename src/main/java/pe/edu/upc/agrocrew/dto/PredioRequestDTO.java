package pe.edu.upc.agrocrew.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import pe.edu.upc.agrocrew.models.FuenteAgua;

import java.math.BigDecimal;

/** Datos para registrar o editar un predio. */
@Data
public class PredioRequestDTO {

    @Schema(example = "Chacra La Esperanza")
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80, message = "El nombre no debe superar 80 caracteres")
    private String nombre;

    @Schema(example = "1204", description = "Id del distrito (ver GET /api/v1/ubigeo/...)")
    @NotNull(message = "El distrito es obligatorio")
    private Long distritoId;

    @Schema(example = "-12.0681")
    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-18.4", message = "La ubicación debe estar dentro del Perú")
    @DecimalMax(value = "0.1", message = "La ubicación debe estar dentro del Perú")
    private Double latitud;

    @Schema(example = "-75.2102")
    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-81.4", message = "La ubicación debe estar dentro del Perú")
    @DecimalMax(value = "-68.6", message = "La ubicación debe estar dentro del Perú")
    private Double longitud;

    @Schema(example = "3250", description = "Opcional. Si no se envía, se usa la altitud de la capital del distrito")
    @Min(value = 0, message = "La altitud no puede ser negativa")
    @Max(value = 6800, message = "La altitud no puede superar 6800 msnm")
    private Integer altitudMsnm;

    @Schema(example = "1.5")
    @NotNull(message = "El área es obligatoria")
    @DecimalMin(value = "0.01", message = "El área debe ser mayor a 0")
    @DecimalMax(value = "100000", message = "El área no puede superar 100000 ha")
    @Digits(integer = 8, fraction = 2, message = "El área admite hasta 2 decimales")
    private BigDecimal areaHa;

    @Schema(example = "SECANO")
    @NotNull(message = "La fuente de agua es obligatoria")
    private FuenteAgua fuenteAgua;

    @Schema(example = "PE-12-0412-0087", description = "Opcional")
    @Size(max = 30, message = "El código catastral no debe superar 30 caracteres")
    private String codigoCatastral;
}
