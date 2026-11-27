package pe.edu.upc.agrocrew.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Nueva contraseña con el token recibido por correo (US05). Misma regla de contraseña que el registro.
 */
@Data
public class RestablecerPasswordRequestDTO {
    @Schema(description = "Token del enlace recibido por correo")
    @NotBlank(message = "El enlace no es válido")
    private String token;

    @Schema(example = "nuevaClave123")
    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$",
            message = "La contraseña debe tener entre 8 y 64 caracteres, con letras y números")
    private String nuevaPassword;
}
