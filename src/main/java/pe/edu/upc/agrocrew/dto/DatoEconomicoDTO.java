package pe.edu.upc.agrocrew.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Dato económico referencial registrado por el administrador (US32).
 */
public record DatoEconomicoDTO(Long id, Long cultivoId, String cultivo, Long departamentoId, String region,
                               BigDecimal precioChacraKg, BigDecimal rendimientoKgHa, BigDecimal costoProduccionHa,
                               String fuente, String periodo, LocalDateTime fechaRegistro,
                               LocalDateTime fechaActualizacion) {
}
