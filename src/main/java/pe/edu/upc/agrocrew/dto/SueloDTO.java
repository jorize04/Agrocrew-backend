package pe.edu.upc.agrocrew.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SueloDTO {
    private Long predioId;
    /** false si no hay clasificación oficial para la ubicación. */
    private boolean disponible;
    private String codigoCumOriginal;
    private GrupoCumDTO grupo;
    private String calidadAgrologica;
    private String limitaciones;
    private String mensaje;
    private String fuente;
}
