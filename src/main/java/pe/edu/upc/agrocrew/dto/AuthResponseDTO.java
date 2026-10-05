package pe.edu.upc.agrocrew.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta del inicio de sesión: token JWT y datos del usuario autenticado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    /** Token JWT que se envía en el header Authorization. */
    private String token;
    /** Tipo de token (Bearer). */
    private String tipo;
    /** Tiempo de vida del token, en milisegundos. */
    private long expiraEnMs;
    /** Datos del usuario autenticado. */
    private UsuarioResponseDTO usuario;
}
