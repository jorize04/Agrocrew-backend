package pe.edu.upc.agrocrew.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cultivos")
@Getter
@Setter
@NoArgsConstructor
public class Cultivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String nombre;

    @Column(length = 100)
    private String nombreCientifico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCultivo tipo;

    /** Duración del ciclo en días; nulo para cultivos permanentes o perennes. */
    private Integer cicloDias;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /** Un cultivo desactivado ya no se recomienda, pero se conserva en evaluaciones anteriores. */
    @Column(nullable = false)
    private Boolean activo = true;

    @OneToOne(mappedBy = "cultivo", cascade = CascadeType.ALL, orphanRemoval = true)
    private RequerimientoCultivo requerimiento;

    /** Grupos CUM en los que el cultivo es apto. */
    @ManyToMany
    @JoinTable(name = "cultivo_grupos_cum",
            joinColumns = @JoinColumn(name = "cultivo_id"),
            inverseJoinColumns = @JoinColumn(name = "grupo_cum_id"))
    private Set<GrupoCum> gruposCum = new HashSet<>();

    /** Mantiene sincronizados ambos lados de la relación uno a uno. */
    public void asignarRequerimiento(RequerimientoCultivo req) {
        this.requerimiento = req;
        req.setCultivo(this);
    }
}
