package pe.edu.upc.agrocrew.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.agrocrew.models.Calificacion;

import java.util.Optional;

public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {

    Optional<Calificacion> findByEvaluacionId(Long evaluacionId);
}
