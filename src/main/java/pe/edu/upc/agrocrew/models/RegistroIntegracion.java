package pe.edu.upc.agrocrew.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Bitácora de cada llamada a un servicio externo (MIDAGRI, ANA, Open-Meteo). */
@Entity
@Table(name = "registros_integracion")
@Getter
@Setter
@NoArgsConstructor
public class RegistroIntegracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ServicioExterno servicio;

    @Column(length = 500)
    private String endpoint;

    private Integer estadoHttp;

    private Long duracionMs;

    @Column(nullable = false)
    private Boolean exito;

    @Column(columnDefinition = "TEXT")
    private String mensajeError;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @PrePersist
    void alRegistrar() {
        this.fecha = LocalDateTime.now();
    }
}
