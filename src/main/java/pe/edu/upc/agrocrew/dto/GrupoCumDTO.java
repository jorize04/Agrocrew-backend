package pe.edu.upc.agrocrew.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Grupo de Capacidad de Uso Mayor (CUM) con su explicación sencilla.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrupoCumDTO {
    /** Código del grupo (A, C, P, F o X). */
    private String codigo;
    /** Nombre del grupo. */
    private String nombre;
    /** Explicación sencilla para el productor. */
    private String descripcionSimple;
    /** Usos recomendados del suelo en este grupo. */
    private String usosRecomendados;
    /** Usos no recomendados del suelo en este grupo. */
    private String usosNoRecomendados;
}
