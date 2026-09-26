package pe.edu.upc.agrocrew.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.agrocrew.dto.ReporteCultivoDTO;
import pe.edu.upc.agrocrew.models.Compatibilidad;
import pe.edu.upc.agrocrew.models.Recomendacion;

import java.time.LocalDateTime;
import java.util.List;

public interface RecomendacionRepository extends JpaRepository<Recomendacion, Long> {

    /**
     * Reporte 3: cultivos que más aparecen en el top 3 de las evaluaciones (con compatibilidad media o alta),
     * con su puntaje promedio, en un rango de fechas y opcionalmente en un departamento (JPQL).
     */
    @Query("SELECT new pe.edu.upc.agrocrew.dto.ReporteCultivoDTO(c.nombre, COUNT(r), AVG(r.puntaje)) "
            + "FROM Recomendacion r JOIN r.cultivo c JOIN r.evaluacion e "
            + "JOIN e.predio p JOIN p.distrito di JOIN di.provincia pr "
            + "WHERE r.compatibilidad <> :baja AND r.posicion <= 3 "
            + "AND e.fecha BETWEEN :desde AND :hasta "
            + "AND (:departamentoId IS NULL OR pr.departamento.id = :departamentoId) "
            + "GROUP BY c.nombre ORDER BY COUNT(r) DESC, c.nombre")
    List<ReporteCultivoDTO> reporteCultivosMasRecomendados(@Param("baja") Compatibilidad baja,
                                                           @Param("desde") LocalDateTime desde,
                                                           @Param("hasta") LocalDateTime hasta,
                                                           @Param("departamentoId") Long departamentoId,
                                                           Pageable pageable);
}
