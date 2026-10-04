package pe.edu.upc.agrocrew.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.edu.upc.agrocrew.dto.ReporteConteoDTO;
import pe.edu.upc.agrocrew.models.Predio;

import java.util.List;
import java.util.Optional;

public interface PredioRepository extends JpaRepository<Predio, Long> {

    /** Predios activos de un usuario, del más reciente al más antiguo. */
    List<Predio> findByUsuarioIdAndActivoTrueOrderByFechaRegistroDesc(Long usuarioId);

    /** Todos los predios activos (para tareas programadas como la revisión de lluvias). */
    List<Predio> findByActivoTrue();

    /** Busca un predio solo si pertenece al usuario y no fue eliminado. */
    Optional<Predio> findByIdAndUsuarioIdAndActivoTrue(Long id, Long usuarioId);

    long countByActivoTrue();

    /** Reporte 1: predios activos registrados por departamento (JPQL). */
    @Query("SELECT new pe.edu.upc.agrocrew.dto.ReporteConteoDTO(d.nombre, COUNT(p)) "
            + "FROM Predio p JOIN p.distrito di JOIN di.provincia pr JOIN pr.departamento d "
            + "WHERE p.activo = true "
            + "GROUP BY d.nombre ORDER BY COUNT(p) DESC, d.nombre")
    List<ReporteConteoDTO> reportePrediosPorDepartamento();
}
