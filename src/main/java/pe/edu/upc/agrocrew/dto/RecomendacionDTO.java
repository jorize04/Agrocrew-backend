package pe.edu.upc.agrocrew.dto;

import pe.edu.upc.agrocrew.models.Compatibilidad;
import pe.edu.upc.agrocrew.models.TipoCultivo;

import java.util.List;

/**
 * Cultivo del ranking de una evaluación, con su puntaje y los factores que lo explican.
 *
 * @param posicion       posición dentro del ranking
 * @param cultivoId      identificador del cultivo
 * @param cultivo        nombre del cultivo
 * @param tipo           tipo de cultivo
 * @param puntaje        puntaje de compatibilidad obtenido
 * @param compatibilidad nivel de compatibilidad: ALTA, MEDIA o BAJA
 * @param factores       factores que explican el puntaje
 */
public record RecomendacionDTO(Integer posicion, Long cultivoId, String cultivo, TipoCultivo tipo,
                               Double puntaje, Compatibilidad compatibilidad, List<FactorDTO> factores) {
}
