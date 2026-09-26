package pe.edu.upc.agrocrew.services;

import pe.edu.upc.agrocrew.dto.PuntoCriticoDTO;
import pe.edu.upc.agrocrew.dto.PuntoCriticoRequestDTO;
import pe.edu.upc.agrocrew.dto.SincronizacionDTO;

public interface PuntoCriticoService {

    /** Descarga los puntos críticos de la ANA, los guarda y genera alertas (TS04). */
    SincronizacionDTO sincronizarConAna();

    /** Registro manual (pruebas o contingencia si la ANA no está disponible). */
    PuntoCriticoDTO registrarManual(PuntoCriticoRequestDTO dto);
}
