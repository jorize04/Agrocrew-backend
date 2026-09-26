package pe.edu.upc.agrocrew.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Condiciones que necesita un cultivo. El motor de reglas compara el predio contra estos rangos. */
@Entity
@Table(name = "requerimientos_cultivo")
@Getter
@Setter
@NoArgsConstructor
public class RequerimientoCultivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "cultivo_id", nullable = false, unique = true)
    private Cultivo cultivo;

    private Integer altitudMin;
    private Integer altitudMax;

    private Double temperaturaMin;
    private Double temperaturaMax;

    private Double precipitacionMinMm;
    private Double precipitacionMaxMm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ToleranciaInundacion toleranciaInundacion;

    @Column(nullable = false)
    private Boolean requiereRiego = false;
}
