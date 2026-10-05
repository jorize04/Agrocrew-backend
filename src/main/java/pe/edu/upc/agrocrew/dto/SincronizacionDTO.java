package pe.edu.upc.agrocrew.dto;

/**
 * Resultado de sincronizar los puntos críticos con la ANA.
 *
 * @param descargados      puntos descargados de la ANA
 * @param nuevos           puntos que no existían y se crearon
 * @param actualizados     puntos existentes que se actualizaron
 * @param alertasGeneradas alertas creadas durante la sincronización
 */
public record SincronizacionDTO(int descargados, int nuevos, int actualizados, int alertasGeneradas) {
}
