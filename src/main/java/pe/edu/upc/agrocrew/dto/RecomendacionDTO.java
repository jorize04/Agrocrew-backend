package pe.edu.upc.agrocrew.dto;

import pe.edu.upc.agrocrew.models.Compatibilidad;
import pe.edu.upc.agrocrew.models.TipoCultivo;

import java.util.List;

public record RecomendacionDTO(Integer posicion, Long cultivoId, String cultivo, TipoCultivo tipo,
                               Double puntaje, Compatibilidad compatibilidad, List<FactorDTO> factores) {
}
