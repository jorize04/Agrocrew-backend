package pe.edu.upc.agrocrew.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.agrocrew.models.Departamento;

import java.util.List;

public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {

    List<Departamento> findAllByOrderByNombreAsc();
}
