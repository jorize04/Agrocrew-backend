package pe.edu.upc.agrocrew.services;

import pe.edu.upc.agrocrew.dto.PredioRequestDTO;
import pe.edu.upc.agrocrew.dto.PredioResponseDTO;
import pe.edu.upc.agrocrew.models.Predio;

import java.util.List;

public interface PredioService {

    PredioResponseDTO registrar(PredioRequestDTO dto);

    List<PredioResponseDTO> listarMisPredios();

    PredioResponseDTO obtener(Long id);

    PredioResponseDTO actualizar(Long id, PredioRequestDTO dto);

    void eliminar(Long id);

    /** Para otros módulos (clima, suelo, riesgo, evaluaciones): devuelve el predio solo si es del usuario actual. */
    Predio obtenerPredioDelUsuarioActual(Long id);
}
