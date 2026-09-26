package pe.edu.upc.agrocrew.dto;

import pe.edu.upc.agrocrew.models.EfectoFactor;
import pe.edu.upc.agrocrew.models.TipoFactor;

public record FactorDTO(TipoFactor factor, String valorPredio, String valorRequerido,
                        EfectoFactor efecto, Double aporte, Integer aporteMaximo, String detalle) {
}
