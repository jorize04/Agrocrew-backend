package pe.edu.upc.agrocrew.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.edu.upc.agrocrew.dto.ErrorResponseDTO;

import java.time.LocalDateTime;

/**
 * Manejador de las excepciones agregadas en el Sprint 3. Tiene prioridad sobre GlobalExceptionHandler
 * (que captura Exception en general) y responde con el mismo formato ErrorResponseDTO.
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RecuperacionExceptionHandler {

    @ExceptionHandler(EnlaceExpiradoException.class)
    public ResponseEntity<ErrorResponseDTO> manejarEnlaceExpirado(EnlaceExpiradoException ex, HttpServletRequest req) {
        HttpStatus estado = HttpStatus.GONE;
        log.warn("{} {} -> {}: {}", req.getMethod(), req.getRequestURI(), estado.value(), ex.getMessage());
        ErrorResponseDTO body = new ErrorResponseDTO(LocalDateTime.now(), estado.value(),
                estado.getReasonPhrase(), ex.getMessage(), req.getRequestURI(), null);
        return ResponseEntity.status(estado).body(body);
    }
}
