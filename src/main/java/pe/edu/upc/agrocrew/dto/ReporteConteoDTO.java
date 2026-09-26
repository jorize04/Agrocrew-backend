package pe.edu.upc.agrocrew.dto;

/** Fila genérica de reporte: una etiqueta (departamento, grupo CUM...) y su cantidad. */
public record ReporteConteoDTO(String etiqueta, Long cantidad) {
}
