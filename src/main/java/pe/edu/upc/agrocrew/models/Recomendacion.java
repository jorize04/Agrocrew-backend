package pe.edu.upc.agrocrew.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Un cultivo dentro del ranking de una evaluación. */
@Entity
@Table(name = "recomendaciones",
        uniqueConstraints = @UniqueConstraint(columnNames = {"evaluacion_id", "cultivo_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Recomendacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluacion_id", nullable = false)
    private Evaluacion evaluacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cultivo_id", nullable = false)
    private Cultivo cultivo;

    @Column(nullable = false)
    private Integer posicion;

    /** Puntaje de 0 a 100. */
    @Column(nullable = false)
    private Double puntaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private Compatibilidad compatibilidad;

    @OneToMany(mappedBy = "recomendacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FactorRecomendacion> factores = new ArrayList<>();

    public void agregarFactor(FactorRecomendacion f) {
        f.setRecomendacion(this);
        this.factores.add(f);
    }
}
