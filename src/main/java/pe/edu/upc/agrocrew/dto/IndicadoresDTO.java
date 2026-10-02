package pe.edu.upc.agrocrew.dto;

/**
 * Indicadores generales de la plataforma y métricas de validación de las hipótesis (US34):
 * - porcentajeEvaluacionesCompletadas: meta 60 %.
 * - porcentajeEvaluacionesPreventivas: evaluaciones hechas sin tener una alerta activa (meta 40 %).
 */
public record IndicadoresDTO(long productores, long asesores, long prediosActivos, long evaluaciones,
                             double porcentajeEvaluacionesCompletadas, double porcentajeEvaluacionesPreventivas,
                             long alertasGeneradas, long alertasNoLeidas, long puntosCriticos, long cultivosActivos) {
}
