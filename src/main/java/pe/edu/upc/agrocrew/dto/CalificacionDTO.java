package pe.edu.upc.agrocrew.dto;

import java.time.LocalDateTime;

/**
 * Calificación guardada de una evaluación (US25).
 *
 * @param actualizada true si reemplazó una calificación anterior del productor
 * @param mensaje     agradecimiento para mostrar al productor
 */
public record CalificacionDTO(Long evaluacionId, Integer puntaje, String comentario, LocalDateTime fecha,
                              LocalDateTime fechaActualizacion, boolean actualizada, String mensaje) {
}
