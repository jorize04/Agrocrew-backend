package pe.edu.upc.agrocrew.dto;

import pe.edu.upc.agrocrew.models.EstadoEvaluacion;
import pe.edu.upc.agrocrew.models.TipoEvaluacion;

import java.time.LocalDateTime;

/** Fila del historial de evaluaciones de un predio. */
public record EvaluacionResumenDTO(Long id, LocalDateTime fecha, TipoEvaluacion tipo, EstadoEvaluacion estado,
                                   String mejorCultivo, Double mejorPuntaje, int cultivosCompatibles) {
}
