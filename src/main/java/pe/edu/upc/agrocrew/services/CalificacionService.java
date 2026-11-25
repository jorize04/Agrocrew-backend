package pe.edu.upc.agrocrew.services;

import pe.edu.upc.agrocrew.dto.CalificacionDTO;
import pe.edu.upc.agrocrew.dto.CalificacionRequestDTO;

import java.util.Optional;

/**
 * Calificación de utilidad de las evaluaciones del productor (US25).
 */
public interface CalificacionService {

    /** Guarda la calificación; si la evaluación ya estaba calificada, la actualiza. */
    CalificacionDTO calificar(Long evaluacionId, CalificacionRequestDTO dto);

    /** Calificación actual de la evaluación, si el productor ya la calificó. */
    Optional<CalificacionDTO> obtener(Long evaluacionId);
}
