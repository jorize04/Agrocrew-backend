package pe.edu.upc.agrocrew.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.agrocrew.models.Distrito;

import java.util.List;
import java.util.Optional;

public interface DistritoRepository extends JpaRepository<Distrito, Long> {

    List<Distrito> findByProvinciaIdOrderByNombreAsc(Long provinciaId);

    /** Sprint 3 (US07): ubica el distrito del predio encontrado en el catastro por su ubigeo INEI. */
    Optional<Distrito> findByUbigeo(String ubigeo);
}
