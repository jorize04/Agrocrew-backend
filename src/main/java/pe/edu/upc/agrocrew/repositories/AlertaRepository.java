package pe.edu.upc.agrocrew.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.agrocrew.models.Alerta;
import pe.edu.upc.agrocrew.models.TipoAlerta;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    List<Alerta> findByPredioUsuarioIdOrderByFechaGeneracionDesc(Long usuarioId);

    List<Alerta> findByPredioUsuarioIdAndLeidaOrderByFechaGeneracionDesc(Long usuarioId, Boolean leida);

    long countByPredioUsuarioIdAndLeidaFalse(Long usuarioId);

    boolean existsByPredioIdAndLeidaFalse(Long predioId);

    Optional<Alerta> findByIdAndPredioUsuarioId(Long id, Long usuarioId);

    /** Evita duplicar la alerta del mismo punto crítico para el mismo predio. */
    boolean existsByPredioIdAndPuntoCriticoIdAndLeidaFalse(Long predioId, Long puntoCriticoId);

    /** Evita más de una alerta de lluvia por predio en el mismo día. */
    boolean existsByPredioIdAndTipoAndFechaGeneracionAfter(Long predioId, TipoAlerta tipo, LocalDateTime desde);

    long countByLeidaFalse();

    /** Reporte 6: alertas generadas por mes y tipo en un año (SQL nativo). Cada fila: [mes, tipo, cantidad]. */
    @Query(value = "SELECT CAST(EXTRACT(MONTH FROM fecha_generacion) AS INTEGER) AS mes, tipo, COUNT(*) AS cantidad "
            + "FROM alertas "
            + "WHERE fecha_generacion >= make_date(:anio, 1, 1) AND fecha_generacion < make_date(:anio + 1, 1, 1) "
            + "GROUP BY 1, 2 ORDER BY 1, 2", nativeQuery = true)
    List<Object[]> reporteAlertasPorMes(@Param("anio") int anio);
}
