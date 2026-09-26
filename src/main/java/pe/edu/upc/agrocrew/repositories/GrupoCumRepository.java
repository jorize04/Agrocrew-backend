package pe.edu.upc.agrocrew.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.agrocrew.models.GrupoCum;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GrupoCumRepository extends JpaRepository<GrupoCum, Long> {

    Optional<GrupoCum> findByCodigo(String codigo);

    List<GrupoCum> findByCodigoIn(Collection<String> codigos);

    List<GrupoCum> findAllByOrderByIdAsc();
}
