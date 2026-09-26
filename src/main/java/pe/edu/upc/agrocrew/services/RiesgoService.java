package pe.edu.upc.agrocrew.services;

import pe.edu.upc.agrocrew.dto.PuntoCriticoDTO;
import pe.edu.upc.agrocrew.dto.RiesgoDTO;

import java.util.List;

public interface RiesgoService {

    /** Exposición del predio a puntos críticos de la ANA (US14). */
    RiesgoDTO consultarRiesgoDePredio(Long predioId);

    /** Puntos críticos alrededor de una coordenada, para el mapa (US17). */
    List<PuntoCriticoDTO> listarCercanos(double latitud, double longitud, double radioKm);
}
