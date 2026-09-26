package pe.edu.upc.agrocrew.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.edu.upc.agrocrew.models.Cultivo;

import java.util.List;

public interface CultivoRepository extends JpaRepository<Cultivo, Long> {

    List<Cultivo> findByActivoTrueOrderByNombreAsc();

    List<Cultivo> findAllByOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    /** Cultivos activos con requerimientos y grupos CUM ya cargados, para el motor de reglas. */
    @EntityGraph(attributePaths = {"requerimiento", "gruposCum"})
    @Query("SELECT c FROM Cultivo c WHERE c.activo = true")
    List<Cultivo> findActivosParaMotor();

    long countByActivoTrue();
}
