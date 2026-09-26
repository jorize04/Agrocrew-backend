package pe.edu.upc.agrocrew.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {

    @Schema(example = "jose.rivera@test.com")
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    private String email;

    @Schema(example = "clave1234")
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
