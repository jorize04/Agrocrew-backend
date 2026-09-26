package pe.edu.upc.agrocrew.services;

import org.springframework.data.domain.Page;
import pe.edu.upc.agrocrew.dto.EvaluacionRequestDTO;
import pe.edu.upc.agrocrew.dto.EvaluacionResponseDTO;
import pe.edu.upc.agrocrew.dto.EvaluacionResumenDTO;

public interface EvaluacionService {

    /** Evalúa el predio: suelo + clima + riesgo + motor de reglas (US18 a US21, US23). */
    EvaluacionResponseDTO evaluar(Long predioId, EvaluacionRequestDTO dto);

    Page<EvaluacionResumenDTO> historial(Long predioId, int pagina, int tamano);

    EvaluacionResponseDTO obtener(Long evaluacionId);
}
