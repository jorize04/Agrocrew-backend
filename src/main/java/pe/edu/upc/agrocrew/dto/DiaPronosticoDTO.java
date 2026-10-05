package pe.edu.upc.agrocrew.dto;

import java.time.LocalDate;

/**
 * Pronóstico del clima para un día.
 *
 * @param fecha           día del pronóstico
 * @param temperaturaMax  temperatura máxima prevista (°C)
 * @param temperaturaMin  temperatura mínima prevista (°C)
 * @param precipitacionMm lluvia prevista (mm)
 */
public record DiaPronosticoDTO(LocalDate fecha, Double temperaturaMax, Double temperaturaMin, Double precipitacionMm) {
}
