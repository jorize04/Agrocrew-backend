package pe.edu.upc.agrocrew.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrupoCumDTO {
    private String codigo;
    private String nombre;
    private String descripcionSimple;
    private String usosRecomendados;
    private String usosNoRecomendados;
}
