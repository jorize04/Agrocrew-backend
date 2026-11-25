package pe.edu.upc.agrocrew.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.agrocrew.dto.MensajeDTO;
import pe.edu.upc.agrocrew.dto.RecuperarPasswordRequestDTO;
import pe.edu.upc.agrocrew.dto.RestablecerPasswordRequestDTO;
import pe.edu.upc.agrocrew.dto.ValidarTokenRequestDTO;
import pe.edu.upc.agrocrew.services.RecuperacionPasswordService;

/**
 * Recuperación de contraseña (US05). Son públicos: /api/v1/auth/** ya está permitido en SecurityConfig.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro e inicio de sesión (públicos)")
public class RecuperacionPasswordController {

    private final RecuperacionPasswordService recuperacionPasswordService;

    @PostMapping("/recuperar")
    @Operation(summary = "Olvidé mi contraseña: enviar enlace de restablecimiento (válido 30 minutos)",
            description = "Responde lo mismo exista o no el correo, para no revelar qué cuentas existen. "
                    + "Sin correo configurado (SPRING_MAIL_HOST), el enlace se escribe en el log del servidor.")
    @ApiResponse(responseCode = "200", description = "Solicitud recibida")
    @ApiResponse(responseCode = "400", description = "Correo con formato inválido")
    public ResponseEntity<MensajeDTO> recuperar(@Valid @RequestBody RecuperarPasswordRequestDTO dto) {
        recuperacionPasswordService.solicitar(dto);
        return ResponseEntity.ok(new MensajeDTO("Si el correo está registrado, te enviamos un enlace para "
                + "restablecer tu contraseña. Revisa tu bandeja de entrada: el enlace vence en 30 minutos."));
    }

    @PostMapping("/restablecer/validar")
    @Operation(summary = "Comprobar el enlace antes de mostrar el formulario de nueva contraseña")
    @ApiResponse(responseCode = "200", description = "El enlace es válido")
    @ApiResponse(responseCode = "400", description = "El enlace no es válido o ya fue usado")
    @ApiResponse(responseCode = "410", description = "El enlace expiró (más de 30 minutos)")
    public ResponseEntity<MensajeDTO> validar(@Valid @RequestBody ValidarTokenRequestDTO dto) {
        recuperacionPasswordService.validar(dto.getToken());
        return ResponseEntity.ok(new MensajeDTO("El enlace es válido."));
    }

    @PostMapping("/restablecer")
    @Operation(summary = "Restablecer la contraseña con el enlace recibido por correo")
    @ApiResponse(responseCode = "200", description = "Contraseña actualizada")
    @ApiResponse(responseCode = "400", description = "Contraseña débil, o enlace no válido o ya usado")
    @ApiResponse(responseCode = "410", description = "El enlace expiró (más de 30 minutos)")
    public ResponseEntity<MensajeDTO> restablecer(@Valid @RequestBody RestablecerPasswordRequestDTO dto) {
        recuperacionPasswordService.restablecer(dto);
        return ResponseEntity.ok(new MensajeDTO("Tu contraseña se actualizó. Ya puedes iniciar sesión."));
    }
}
