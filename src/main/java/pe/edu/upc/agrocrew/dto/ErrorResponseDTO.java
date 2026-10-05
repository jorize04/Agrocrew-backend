package pe.edu.upc.agrocrew.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato común de las respuestas de error de la API.
 *
 * <p>Los campos nulos se omiten del JSON.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDTO {
    /** Momento en que ocurrió el error. */
    private LocalDateTime fecha;
    /** Código de estado HTTP. */
    private int estado;
    /** Texto corto que describe el tipo de error. */
    private String error;
    /** Mensaje explicativo del error. */
    private String mensaje;
    /** Ruta de la solicitud que falló. */
    private String ruta;
    /** Información adicional por campo; se omite cuando no hay. */
    private Map<String, String> detalles;
}
