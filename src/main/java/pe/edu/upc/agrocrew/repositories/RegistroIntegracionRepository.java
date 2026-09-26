package pe.edu.upc.agrocrew.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.agrocrew.models.RegistroIntegracion;

import java.util.List;

public interface RegistroIntegracionRepository extends JpaRepository<RegistroIntegracion, Long> {

    List<RegistroIntegracion> findTop50ByOrderByFechaDesc();
}
