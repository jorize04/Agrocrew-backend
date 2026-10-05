package pe.edu.upc.agrocrew.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Datos para registrar un nuevo usuario (productor o asesor).
 */
@Data
public class RegistroRequestDTO {

    /** Nombres del usuario (máximo 80 caracteres). */
    @Schema(example = "José")
    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 80)
    private String nombres;

    /** Apellidos del usuario (máximo 80 caracteres). */
    @Schema(example = "Rivera")
    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 80)
    private String apellidos;

    /** Correo electrónico; se usa para iniciar sesión (máximo 120 caracteres). */
    @Schema(example = "jose.rivera@test.com")
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 120)
    private String email;

    /** Contraseña de 8 a 64 caracteres, con letras y números. */
    @Schema(example = "clave1234")
    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$",
            message = "La contraseña debe tener entre 8 y 64 caracteres e incluir letras y números")
    private String password;

    /** Teléfono de 9 dígitos; es opcional. */
    @Schema(example = "987654321")
    @Pattern(regexp = "^\\d{9}$", message = "El teléfono debe tener 9 dígitos")
    private String telefono;

    /** Organización a la que pertenece; es opcional (máximo 120 caracteres). */
    @Schema(example = "Agencia Agraria Huancayo")
    @Size(max = 120)
    private String organizacion;

    /** Tipo de usuario: PRODUCTOR o ASESOR. */
    @NotBlank(message = "El tipo de usuario es obligatorio")
    @Pattern(regexp = "PRODUCTOR|ASESOR", message = "El tipo de usuario debe ser PRODUCTOR o ASESOR")
    @Schema(example = "PRODUCTOR", allowableValues = {"PRODUCTOR", "ASESOR"})
    private String tipoUsuario;
}
