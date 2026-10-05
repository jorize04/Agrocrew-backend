package pe.edu.upc.agrocrew.dto;

/**
 * Cantidad de alertas de un tipo generadas en un mes del año consultado.
 *
 * @param mes      número del mes
 * @param tipo     tipo de alerta
 * @param cantidad cantidad de alertas
 */
public record ReporteMensualDTO(Integer mes, String tipo, Long cantidad) {
}
