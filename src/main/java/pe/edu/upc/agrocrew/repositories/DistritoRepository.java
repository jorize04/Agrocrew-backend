package pe.edu.upc.agrocrew.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.agrocrew.models.Distrito;

import java.util.List;

public interface DistritoRepository extends JpaRepository<Distrito, Long> {

    List<Distrito> findByProvinciaIdOrderByNombreAsc(Long provinciaId);
}
