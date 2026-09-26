package pe.edu.upc.agrocrew.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upc.agrocrew.models.NivelRiesgo;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiesgoDTO {
    private Long predioId;
    /** Mayor nivel de riesgo entre los puntos críticos dentro del radio; BAJO si no hay ninguno. */
    private NivelRiesgo nivelRiesgo;
    private double radioKm;
    private PuntoCriticoDTO puntoMasCercano;
    private List<PuntoCriticoDTO> puntosCercanos;
    private String mensaje;
}
