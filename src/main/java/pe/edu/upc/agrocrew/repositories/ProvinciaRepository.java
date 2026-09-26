package pe.edu.upc.agrocrew.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.agrocrew.models.Provincia;

import java.util.List;

public interface ProvinciaRepository extends JpaRepository<Provincia, Long> {

    List<Provincia> findByDepartamentoIdOrderByNombreAsc(Long departamentoId);
}
