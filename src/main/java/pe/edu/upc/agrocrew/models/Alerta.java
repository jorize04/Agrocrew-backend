package pe.edu.upc.agrocrew.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "alertas")
@Getter
@Setter
@NoArgsConstructor
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "predio_id", nullable = false)
    private Predio predio;

    /** Nulo cuando la alerta es por clima. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "punto_critico_id")
    private PuntoCritico puntoCritico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAlerta tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private NivelRiesgo nivel;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(columnDefinition = "TEXT")
    private String accionesSugeridas;

    @Column(nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(nullable = false)
    private Boolean leida = false;

    private LocalDateTime fechaLectura;

    @PrePersist
    void alRegistrar() {
        this.fechaGeneracion = LocalDateTime.now();
        if (this.leida == null) {
            this.leida = false;
        }
    }
}
