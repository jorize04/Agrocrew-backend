package pe.edu.upc.agrocrew.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistroRequestDTO {

    @Schema(example = "José")
    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 80)
    private String nombres;

    @Schema(example = "Rivera")
    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 80)
    private String apellidos;

    @Schema(example = "jose.rivera@test.com")
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 120)
    private String email;

    @Schema(example = "clave1234")
    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$",
            message = "La contraseña debe tener entre 8 y 64 caracteres e incluir letras y números")
    private String password;

    @Schema(example = "987654321")
    @Pattern(regexp = "^\\d{9}$", message = "El teléfono debe tener 9 dígitos")
    private String telefono;

    @Schema(example = "Agencia Agraria Huancayo")
    @Size(max = 120)
    private String organizacion;

    @NotBlank(message = "El tipo de usuario es obligatorio")
    @Pattern(regexp = "PRODUCTOR|ASESOR", message = "El tipo de usuario debe ser PRODUCTOR o ASESOR")
    @Schema(example = "PRODUCTOR", allowableValues = {"PRODUCTOR", "ASESOR"})
    private String tipoUsuario;
}
