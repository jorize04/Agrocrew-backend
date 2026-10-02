package pe.edu.upc.agrocrew.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Grupo de Capacidad de Uso Mayor de las tierras (A, C, P, F, X),
 * según el Reglamento de Clasificación de Tierras del MIDAGRI.
 */
@Entity
@Table(name = "grupos_cum")
@Getter
@Setter
@NoArgsConstructor
public class GrupoCum {
    /** Identificador único del grupo CUM, autogenerado por la base de datos. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Código oficial del grupo CUM según la clasificación de MIDAGRI (2 caracteres).
     * Debe ser único, ya que identifica cada uno de los 5 grupos del catálogo.
     */
    @Column(nullable = false, unique = true, length = 2)
    private String codigo;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcionSimple;

    @Column(columnDefinition = "TEXT")
    private String usosRecomendados;

    @Column(columnDefinition = "TEXT")
    private String usosNoRecomendados;
}
