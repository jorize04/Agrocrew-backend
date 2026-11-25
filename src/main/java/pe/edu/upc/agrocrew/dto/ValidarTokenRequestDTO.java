package pe.edu.upc.agrocrew.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Token del enlace de restablecimiento, para comprobarlo antes de mostrar el formulario (US05).
 */
@Data
public class ValidarTokenRequestDTO {
    @NotBlank(message = "El enlace no es válido")
    private String token;
}
