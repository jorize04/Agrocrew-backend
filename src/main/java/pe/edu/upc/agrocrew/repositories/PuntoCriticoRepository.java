package pe.edu.upc.agrocrew.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.edu.upc.agrocrew.models.PuntoCritico;

import java.util.List;
import java.util.Optional;

public interface PuntoCriticoRepository extends JpaRepository<PuntoCritico, Long> {

    Optional<PuntoCritico> findByCodigoAna(String codigoAna);

    /** Primer filtro rápido por un recuadro de coordenadas; la distancia exacta se calcula en Java. */
    List<PuntoCritico> findByLatitudBetweenAndLongitudBetween(Double latMin, Double latMax,
                                                              Double lngMin, Double lngMax);

    /**
     * Reporte 4: puntos críticos de la ANA por departamento y nivel (SQL nativo de PostgreSQL).
     * El departamento es la primera parte de "ubicacion" (ej. "Áncash / Huarmey / Huarmey").
     * Cada fila: [departamento, nivel_riesgo, cantidad].
     */
    @Query(value = "SELECT COALESCE(NULLIF(split_part(ubicacion, ' / ', 1), ''), 'SIN UBICACION') AS departamento, "
            + "nivel_riesgo, COUNT(*) AS cantidad "
            + "FROM puntos_criticos GROUP BY 1, 2 ORDER BY 1, 2", nativeQuery = true)
    List<Object[]> reportePuntosCriticosPorDepartamento();
}
