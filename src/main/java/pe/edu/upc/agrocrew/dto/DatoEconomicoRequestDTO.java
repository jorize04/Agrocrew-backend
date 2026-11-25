package pe.edu.upc.agrocrew.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Registro o edición de un dato económico referencial (US32). La región es un departamento.
 */
@Data
public class DatoEconomicoRequestDTO {
    @Schema(example = "1")
    @NotNull(message = "El cultivo es obligatorio")
    private Long cultivoId;

    @Schema(example = "12", description = "Id del departamento (región). Ver GET /api/v1/ubigeo/departamentos")
    @NotNull(message = "La región es obligatoria")
    private Long departamentoId;

    @Schema(example = "1.20", description = "Precio en chacra, en soles por kg")
    @NotNull(message = "El precio en chacra es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "El precio admite hasta 2 decimales")
    private BigDecimal precioChacraKg;

    @Schema(example = "15000", description = "Rendimiento en kg por hectárea")
    @NotNull(message = "El rendimiento es obligatorio")
    @DecimalMin(value = "0.01", message = "El rendimiento debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "El rendimiento admite hasta 2 decimales")
    private BigDecimal rendimientoKgHa;

    @Schema(example = "9500", description = "Costo de producción en soles por hectárea")
    @NotNull(message = "El costo de producción es obligatorio")
    @DecimalMin(value = "0.00", message = "El costo no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El costo admite hasta 2 decimales")
    private BigDecimal costoProduccionHa;

    @Schema(example = "MIDAGRI - Sistema Integrado de Estadística Agraria (SIEA)")
    @NotBlank(message = "La fuente es obligatoria")
    @Size(max = 150, message = "La fuente no debe superar 150 caracteres")
    private String fuente;

    @Schema(example = "2025", description = "Año (2025) o año y mes (2025-06)")
    @NotBlank(message = "El periodo es obligatorio")
    @Pattern(regexp = "^\\d{4}(-(0[1-9]|1[0-2]))?$", message = "El periodo debe tener el formato AAAA o AAAA-MM")
    private String periodo;
}
