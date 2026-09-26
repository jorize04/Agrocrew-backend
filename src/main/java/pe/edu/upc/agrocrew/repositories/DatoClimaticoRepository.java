package pe.edu.upc.agrocrew.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.agrocrew.models.DatoClimatico;

import java.util.Optional;

public interface DatoClimaticoRepository extends JpaRepository<DatoClimatico, Long> {

    Optional<DatoClimatico> findFirstByPredioIdOrderByFechaConsultaDesc(Long predioId);
}
