package pe.edu.upc.agrocrew.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Datos de un usuario del sistema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {
    /** Identificador del usuario. */
    private Long id;
    /** Nombres del usuario. */
    private String nombres;
    /** Apellidos del usuario. */
    private String apellidos;
    /** Correo electrónico del usuario. */
    private String email;
    /** Teléfono de contacto. */
    private String telefono;
    /** Organización a la que pertenece. */
    private String organizacion;
    /** Rol del usuario en el sistema. */
    private String rol;
    /** Indica si la cuenta está activa. */
    private Boolean activo;
    /** Fecha y hora de registro. */
    private LocalDateTime fechaRegistro;
}
