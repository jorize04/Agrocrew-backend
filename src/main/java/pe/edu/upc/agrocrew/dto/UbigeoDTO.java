package pe.edu.upc.agrocrew.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Elemento de los combos de departamento, provincia y distrito. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UbigeoDTO {
    private Long id;
    private String ubigeo;
    private String nombre;
}
