package pe.edu.upc.agrocrew.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Resultado de evaluar un predio. Guarda una "foto" de los datos usados en ese momento
 * (suelo, clima, riesgo) para que el historial no cambie si luego se edita el predio.
 */
@Entity
@Table(name = "evaluaciones")
@Getter
@Setter
@NoArgsConstructor
public class Evaluacion {

    /** Identificador único de la evaluación, autogenerado por la base de datos. */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Predio sobre el cual se realiza la evaluación.
     * Relación obligatoria: toda evaluación pertenece a un único predio.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "predio_id", nullable = false)
    private Predio predio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_cum_id")
    private GrupoCum grupoCum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cultivo_consultado_id")
    private Cultivo cultivoConsultado;
    /**
     * Punto crítico de riesgo hídrico (ANA) más cercano relacionado con esta evaluación.
     * Nulo si no hay puntos críticos dentro del radio configurado.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "punto_critico_id")
    private PuntoCritico puntoCritico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoEvaluacion tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoEvaluacion estado;

    @Column(nullable = false)
    private LocalDateTime fecha;

    // ---- Foto de los datos usados
    @Column(length = 40)
    private String cumCodigoOriginal;

    @Column(length = 10)
    private String calidadAgrologica;

    @Column(length = 100)
    private String limitacionesSuelo;

    private Integer altitudMsnm;
    private Double temperaturaMedia;
    private Double precipitacionAnualMm;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private FuenteAgua fuenteAgua;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private NivelRiesgo nivelRiesgoHidrico;

    private Double distanciaPuntoCriticoKm;

    /** true si el predio tenía una alerta sin leer al momento de evaluar (métrica de consulta preventiva). */
    @Column(nullable = false)
    private Boolean alertaActiva = false;

    /** Fuentes que no respondieron, separadas por coma. Ej: "MIDAGRI,OPEN_METEO". */
    @Column(length = 100)
    private String fuentesFaltantes;

    @Column(columnDefinition = "TEXT")
    private String explicacion;

    /** En el Sprint 1 la explicación se genera con plantilla; con IA (Parte 2) será true. */
    @Column(nullable = false)
    private Boolean explicacionPorIa = false;

    @Column(length = 60)
    private String modeloIa;

    @OneToMany(mappedBy = "evaluacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("posicion ASC")
    private List<Recomendacion> recomendaciones = new ArrayList<>();

    @PrePersist
    void alRegistrar() {
        this.fecha = LocalDateTime.now();
    }

    public void agregarRecomendacion(Recomendacion r) {
        r.setEvaluacion(this);
        this.recomendaciones.add(r);
    }
}
