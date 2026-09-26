package pe.edu.upc.agrocrew.services;

import pe.edu.upc.agrocrew.dto.ClimaDTO;
import pe.edu.upc.agrocrew.integraciones.OpenMeteoClient;
import pe.edu.upc.agrocrew.models.Predio;

public interface ClimaService {

    /** Clima anual y pronóstico de un predio del usuario actual (US13). */
    ClimaDTO obtenerClimaDePredio(Long predioId);

    /** Clima de los últimos 12 meses; si Open-Meteo falla usa el último dato guardado. Para el motor. */
    OpenMeteoClient.ClimaAnual obtenerClimaAnual(Predio predio);
}
