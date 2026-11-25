package pe.edu.upc.agrocrew.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.agrocrew.models.DatoEconomico;

import java.util.List;
import java.util.Optional;

public interface DatoEconomicoRepository extends JpaRepository<DatoEconomico, Long> {

    boolean existsByCultivoIdAndDepartamentoIdAndPeriodo(Long cultivoId, Long departamentoId, String periodo);

    boolean existsByCultivoIdAndDepartamentoIdAndPeriodoAndIdNot(Long cultivoId, Long departamentoId,
                                                                 String periodo, Long id);

    /** El dato más reciente de un cultivo en una región (el periodo AAAA o AAAA-MM ordena bien como texto). */
    Optional<DatoEconomico> findFirstByCultivoIdAndDepartamentoIdOrderByPeriodoDesc(Long cultivoId, Long departamentoId);

    /** Listado para el administrador, con filtros opcionales por cultivo y región. */
    @Query("SELECT d FROM DatoEconomico d JOIN FETCH d.cultivo c JOIN FETCH d.departamento dep "
            + "WHERE (:cultivoId IS NULL OR c.id = :cultivoId) "
            + "AND (:departamentoId IS NULL OR dep.id = :departamentoId) "
            + "ORDER BY c.nombre, dep.nombre, d.periodo DESC")
    List<DatoEconomico> buscar(@Param("cultivoId") Long cultivoId, @Param("departamentoId") Long departamentoId);
}
