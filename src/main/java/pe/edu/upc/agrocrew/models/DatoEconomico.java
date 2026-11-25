package pe.edu.upc.agrocrew.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Dato económico referencial de un cultivo en una región (US32), usado para la rentabilidad (US22).
 * La región es el departamento. Solo hay un registro por cultivo, región y periodo.
 */
@Entity
@Table(name = "datos_economicos",
        uniqueConstraints = @UniqueConstraint(name = "uk_dato_economico_cultivo_region_periodo",
                columnNames = {"cultivo_id", "departamento_id", "periodo"}))
@Getter
@Setter
@NoArgsConstructor
public class DatoEconomico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cultivo_id", nullable = false)
    private Cultivo cultivo;

    /** Región del dato (departamento). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "departamento_id", nullable = false)
    private Departamento departamento;

    /** Precio en chacra, en soles por kilogramo. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioChacraKg;

    /** Rendimiento en kilogramos por hectárea. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal rendimientoKgHa;

    /** Costo de producción en soles por hectárea. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoProduccionHa;

    /** Fuente del dato (por ejemplo, MIDAGRI - SIEA). */
    @Column(nullable = false, length = 150)
    private String fuente;

    /** Periodo del dato: año (2025) o año y mes (2025-06). */
    @Column(nullable = false, length = 7)
    private String periodo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    private LocalDateTime fechaActualizacion;

    @PrePersist
    void alRegistrar() {
        this.fechaRegistro = LocalDateTime.now();
    }

    @PreUpdate
    void alActualizar() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
