package pe.edu.upc.agrocrew.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Datos del perfil que el usuario puede editar (US04). El correo y el rol no se cambian aquí. */
@Data
public class PerfilRequestDTO {

    @Schema(example = "José")
    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 80)
    private String nombres;

    @Schema(example = "Rivera")
    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 80)
    private String apellidos;

    @Schema(example = "987654321")
    @Pattern(regexp = "^\\d{9}$", message = "El teléfono debe tener 9 dígitos")
    private String telefono;

    @Schema(example = "Agencia Agraria Huancayo")
    @Size(max = 120)
    private String organizacion;
}
