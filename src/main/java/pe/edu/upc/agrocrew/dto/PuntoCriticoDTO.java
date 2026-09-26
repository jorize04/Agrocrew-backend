package pe.edu.upc.agrocrew.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upc.agrocrew.models.NivelRiesgo;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PuntoCriticoDTO {
    private Long id;
    private String codigoAna;
    private String descripcion;
    private String tipoPeligro;
    private NivelRiesgo nivelRiesgo;
    private Double latitud;
    private Double longitud;
    private String ubicacion;
    /** Distancia al predio o punto consultado (km); nulo si no aplica. */
    private Double distanciaKm;
}
