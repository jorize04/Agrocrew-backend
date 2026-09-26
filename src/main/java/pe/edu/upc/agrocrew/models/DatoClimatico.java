package pe.edu.upc.agrocrew.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Último clima obtenido para un predio. Sirve de respaldo si Open-Meteo no responde. */
@Entity
@Table(name = "datos_climaticos")
@Getter
@Setter
@NoArgsConstructor
public class DatoClimatico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "predio_id", nullable = false)
    private Predio predio;

    @Column(nullable = false)
    private LocalDateTime fechaConsulta;

    /** Temperatura media de los últimos 12 meses (°C). */
    private Double temperaturaMedia;

    /** Lluvia acumulada de los últimos 12 meses (mm). */
    private Double precipitacionAnualMm;

    /** Pronóstico de 7 días guardado como JSON. */
    @Column(columnDefinition = "TEXT")
    private String pronostico;
}
