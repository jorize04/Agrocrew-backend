package pe.edu.upc.agrocrew.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Punto crítico de riesgo hídrico (inundación, desborde, etc.) sincronizado desde la ANA. */
@Entity
@Table(name = "puntos_criticos")
@Getter
@Setter
@NoArgsConstructor
public class PuntoCritico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String codigoAna;

    @Column(length = 500)
    private String descripcion;

    @Column(length = 100)
    private String tipoPeligro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private NivelRiesgo nivelRiesgo;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    @Column(length = 200)
    private String ubicacion;

    @Column(nullable = false)
    private LocalDateTime fechaSincronizacion;
}
