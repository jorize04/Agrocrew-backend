package pe.edu.upc.agrocrew.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Detalle de un factor (suelo, altitud, etc.) que explica el puntaje de un cultivo. */
@Entity
@Table(name = "factores_recomendacion")
@Getter
@Setter
@NoArgsConstructor
public class FactorRecomendacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recomendacion_id", nullable = false)
    private Recomendacion recomendacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoFactor factor;

    @Column(length = 60)
    private String valorPredio;

    @Column(length = 60)
    private String valorRequerido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EfectoFactor efecto;

    @Column(nullable = false)
    private Double aporte;

    @Column(length = 200)
    private String detalle;
}
