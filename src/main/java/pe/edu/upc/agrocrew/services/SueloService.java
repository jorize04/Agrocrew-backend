package pe.edu.upc.agrocrew.services;

import pe.edu.upc.agrocrew.dto.SueloDTO;

public interface SueloService {

    /** Capacidad de Uso Mayor del predio según MIDAGRI (US11 y US12). */
    SueloDTO consultarSueloDePredio(Long predioId);
}
