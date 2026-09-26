package pe.edu.upc.agrocrew.services;

import pe.edu.upc.agrocrew.dto.AlertaDTO;
import pe.edu.upc.agrocrew.models.Predio;

import java.util.List;

public interface AlertaService {

    /** Crea alertas si el predio está cerca de puntos críticos de riesgo ALTO o MUY_ALTO. Devuelve cuántas creó. */
    int generarAlertasRiesgo(Predio predio);

    /** Revisa todos los predios activos (se usa después de sincronizar la ANA). */
    int generarAlertasRiesgoParaTodos();

    /** Revisa el pronóstico de todos los predios y alerta si se espera lluvia intensa. */
    int revisarLluviasIntensas();

    List<AlertaDTO> listarMisAlertas(Boolean leida);

    long contarNoLeidas();

    void marcarComoLeida(Long alertaId);
}
