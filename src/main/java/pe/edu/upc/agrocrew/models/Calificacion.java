package pe.edu.upc.agrocrew.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Calificación de utilidad que el productor da a una evaluación (US25).
 * Hay una sola por evaluación: si el productor vuelve a calificar, se actualiza la anterior.
 */
@Entity
@Table(name = "calificaciones")
@Getter
@Setter
@NoArgsConstructor
public class Calificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluacion_id", nullable = false, unique = true)
    private Evaluacion evaluacion;

    /** De 1 (nada útil) a 5 (muy útil). */
    @Column(nullable = false)
    private Integer puntaje;

    @Column(length = 500)
    private String comentario;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    private LocalDateTime fechaActualizacion;

    @PrePersist
    void alRegistrar() {
        this.fecha = LocalDateTime.now();
    }

    @PreUpdate
    void alActualizar() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
