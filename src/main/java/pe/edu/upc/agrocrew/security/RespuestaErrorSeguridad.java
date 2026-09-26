package pe.edu.upc.agrocrew.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import pe.edu.upc.agrocrew.dto.ErrorResponseDTO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Responde 401 (sin token o token inválido) y 403 (rol insuficiente) con el mismo
 * formato JSON que GlobalExceptionHandler. Estos errores ocurren en los filtros,
 * antes de llegar a los controladores, por eso se manejan aquí.
 */
@Component
@RequiredArgsConstructor
public class RespuestaErrorSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        escribir(response, request, HttpStatus.UNAUTHORIZED, "Debe iniciar sesión o el token no es válido");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        escribir(response, request, HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta acción");
    }

    private void escribir(HttpServletResponse response, HttpServletRequest request,
                          HttpStatus estado, String mensaje) throws IOException {
        ErrorResponseDTO body = new ErrorResponseDTO(LocalDateTime.now(), estado.value(),
                estado.getReasonPhrase(), mensaje, request.getRequestURI(), null);
        response.setStatus(estado.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
