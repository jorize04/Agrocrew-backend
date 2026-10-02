package pe.edu.upc.agrocrew.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.agrocrew.dto.ReporteConteoDTO;
import pe.edu.upc.agrocrew.dto.ReporteNivelRiesgoDTO;
import pe.edu.upc.agrocrew.models.EstadoEvaluacion;
import pe.edu.upc.agrocrew.models.Evaluacion;

import java.util.List;
import java.util.Optional;

public interface EvaluacionRepository extends JpaRepository<Evaluacion, Long> {

    Page<Evaluacion> findByPredioIdOrderByFechaDesc(Long predioId, Pageable pageable);

    Optional<Evaluacion> findByIdAndPredioUsuarioId(Long id, Long usuarioId);

    long countByEstado(EstadoEvaluacion estado);

    long countByAlertaActivaFalse();

    /** Reporte 2: evaluaciones por grupo de Capacidad de Uso Mayor, opcionalmente de un departamento (JPQL). */
    @Query("SELECT new pe.edu.upc.agrocrew.dto.ReporteConteoDTO(COALESCE(g.codigo, 'SIN CLASIFICACION'), COUNT(e)) "
            + "FROM Evaluacion e LEFT JOIN e.grupoCum g "
            + "JOIN e.predio p JOIN p.distrito di JOIN di.provincia pr "
            + "WHERE (:departamentoId IS NULL OR pr.departamento.id = :departamentoId) "
            + "GROUP BY g.codigo ORDER BY COUNT(e) DESC, g.codigo")
    List<ReporteConteoDTO> reporteEvaluacionesPorGrupoCum(@Param("departamentoId") Long departamentoId);

    /** Reporte 5: predios evaluados por departamento y nivel de riesgo hídrico (JPQL). */
    @Query("SELECT new pe.edu.upc.agrocrew.dto.ReporteNivelRiesgoDTO(d.nombre, e.nivelRiesgoHidrico, COUNT(DISTINCT p.id)) "
            + "FROM Evaluacion e JOIN e.predio p JOIN p.distrito di JOIN di.provincia pr JOIN pr.departamento d "
            + "GROUP BY d.nombre, e.nivelRiesgoHidrico ORDER BY d.nombre, e.nivelRiesgoHidrico")
    List<ReporteNivelRiesgoDTO> reportePrediosPorNivelRiesgo();
}