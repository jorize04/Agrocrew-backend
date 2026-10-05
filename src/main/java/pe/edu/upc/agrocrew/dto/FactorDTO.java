package pe.edu.upc.agrocrew.dto;

import pe.edu.upc.agrocrew.models.EfectoFactor;
import pe.edu.upc.agrocrew.models.TipoFactor;

/**
 * Factor (suelo, altitud, etc.) que explica el puntaje de un cultivo.
 *
 * @param factor         tipo de factor evaluado
 * @param valorPredio    valor del factor en el predio
 * @param valorRequerido valor que requiere el cultivo
 * @param efecto         efecto sobre el puntaje: FAVORABLE, NEUTRO o LIMITANTE
 * @param aporte         puntos que el factor aportó al puntaje
 * @param aporteMaximo   máximo de puntos que el factor podía aportar
 * @param detalle        explicación en texto del resultado del factor
 */
public record FactorDTO(TipoFactor factor, String valorPredio, String valorRequerido,
                        EfectoFactor efecto, Double aporte, Integer aporteMaximo, String detalle) {
}
