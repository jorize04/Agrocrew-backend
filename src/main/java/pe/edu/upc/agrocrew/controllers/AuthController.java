package pe.edu.upc.agrocrew.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.agrocrew.dto.AuthResponseDTO;
import pe.edu.upc.agrocrew.dto.LoginRequestDTO;
import pe.edu.upc.agrocrew.dto.RegistroRequestDTO;
import pe.edu.upc.agrocrew.dto.UsuarioResponseDTO;
import pe.edu.upc.agrocrew.services.AuthService;

/**
 * Endpoints públicos de autenticación: registro e inicio de sesión.
 *
 * <p>No requieren token. El inicio de sesión devuelve el JWT que se usa en el resto de la API.</p>
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro e inicio de sesión (públicos)")
public class AuthController {

    private final AuthService authService;

    /**
     * Registra un nuevo usuario con rol productor o asesor.
     *
     * @param dto datos de registro validados
     * @return el usuario creado con código 201; 409 si el correo ya está registrado
     */
    @PostMapping("/register")
    @Operation(summary = "Registrar un productor o asesor")
    @ApiResponse(responseCode = "201", description = "Usuario registrado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "409", description = "El correo ya está registrado")
    public ResponseEntity<UsuarioResponseDTO> registrar(@Valid @RequestBody RegistroRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(dto));
    }

    /**
     * Inicia sesión con correo y contraseña.
     *
     * @param dto credenciales del usuario
     * @return el token JWT con código 200; 401 si las credenciales son incorrectas
     */
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener el token JWT")
    @ApiResponse(responseCode = "200", description = "Sesión iniciada, devuelve el token")
    @ApiResponse(responseCode = "401", description = "Correo o contraseña incorrectos")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }
}
