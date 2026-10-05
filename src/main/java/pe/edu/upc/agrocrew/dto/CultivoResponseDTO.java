package pe.edu.upc.agrocrew.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upc.agrocrew.models.TipoCultivo;

import java.util.List;

/**
 * Cultivo del catálogo con sus grupos CUM aptos y sus requerimientos.
 */
@Data
@NoArgsConstructor
public class CultivoResponseDTO {
    /** Identificador del cultivo. */
    private Long id;
    /** Nombre común del cultivo. */
    private String nombre;
    /** Nombre científico. */
    private String nombreCientifico;
    /** Tipo de cultivo. */
    private TipoCultivo tipo;
    /** Duración del ciclo en días; vacío en cultivos permanentes. */
    private Integer cicloDias;
    /** Descripción breve del cultivo. */
    private String descripcion;
    /** Indica si el cultivo está activo en el catálogo. */
    private Boolean activo;
    /** Códigos de los grupos CUM aptos. */
    private List<String> gruposCum;
    /** Requerimientos del cultivo. */
    private RequerimientoCultivoDTO requerimiento;
}
