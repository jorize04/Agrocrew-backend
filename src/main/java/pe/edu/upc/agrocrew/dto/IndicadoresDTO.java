package pe.edu.upc.agrocrew.dto;

/**
 * Indicadores generales de la plataforma y métricas de validación de las hipótesis (US34):
 * - porcentajeEvaluacionesCompletadas: meta 60 %.
 * - porcentajeEvaluacionesPreventivas: evaluaciones hechas sin tener una alerta activa (meta 40 %).
 * - porcentajeCalificacionesUtiles: calificaciones de 4 o 5 sobre el total de calificaciones (Sprint 3, US25).
 */
public record IndicadoresDTO(long productores, long asesores, long prediosActivos, long evaluaciones,
                             double porcentajeEvaluacionesCompletadas, double porcentajeEvaluacionesPreventivas,
                             long alertasGeneradas, long alertasNoLeidas, long puntosCriticos, long cultivosActivos,
                             long calificaciones, double porcentajeCalificacionesUtiles) {
}
