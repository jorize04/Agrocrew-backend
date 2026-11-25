package pe.edu.upc.agrocrew.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Solicitud del enlace de restablecimiento ("Olvidé mi contraseña", US05).
 */
@Data
public class RecuperarPasswordRequestDTO {
    @Schema(example = "jose.rivera@test.com")
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    private String email;
}
