package pe.edu.upc.agrocrew.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upc.agrocrew.models.TipoCultivo;

import java.util.List;

@Data
@NoArgsConstructor
public class CultivoResponseDTO {
    private Long id;
    private String nombre;
    private String nombreCientifico;
    private TipoCultivo tipo;
    private Integer cicloDias;
    private String descripcion;
    private Boolean activo;
    private List<String> gruposCum;
    private RequerimientoCultivoDTO requerimiento;
}
