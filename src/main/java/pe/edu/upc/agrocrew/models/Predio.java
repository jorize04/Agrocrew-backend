package pe.edu.upc.agrocrew.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "predios")
@Getter
@Setter
@NoArgsConstructor
public class Predio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "distrito_id", nullable = false)
    private Distrito distrito;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(length = 30)
    private String codigoCatastral;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    private Integer altitudMsnm;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal areaHa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FuenteAgua fuenteAgua;

    /** Eliminación lógica: un predio eliminado queda con activo = false. */
    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    private LocalDateTime fechaActualizacion;

    @PrePersist
    void alRegistrar() {
        this.fechaRegistro = LocalDateTime.now();
        if (this.activo == null) {
            this.activo = true;
        }
    }

    @PreUpdate
    void alActualizar() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
