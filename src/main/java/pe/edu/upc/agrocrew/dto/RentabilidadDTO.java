package pe.edu.upc.agrocrew.dto;

import java.math.BigDecimal;

/**
 * Rentabilidad referencial de un cultivo en la región del predio (US22).
 * Si no hay datos económicos, disponible = false y las cifras van en null.
 *
 * <p>ingresoHa = precioChacraKg × rendimientoKgHa; margenHa = ingresoHa − costoHa. Montos en soles (PEN).</p>
 */
public record RentabilidadDTO(Long predioId, Long cultivoId, String cultivo, String region, boolean disponible,
                              BigDecimal ingresoHa, BigDecimal costoHa, BigDecimal margenHa,
                              BigDecimal precioChacraKg, BigDecimal rendimientoKgHa, String moneda,
                              String fuente, String periodo, String mensaje, String aviso) {
}
