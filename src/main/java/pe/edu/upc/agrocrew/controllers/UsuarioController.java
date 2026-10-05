package pe.edu.upc.agrocrew.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.agrocrew.dto.PerfilRequestDTO;
import pe.edu.upc.agrocrew.dto.UsuarioResponseDTO;
import pe.edu.upc.agrocrew.services.UsuarioService;

/**
 * Perfil del usuario autenticado.
 *
 * <p>Permite consultar y editar los datos personales de quien tiene la sesión iniciada.</p>
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Perfil del usuario autenticado")
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Obtiene el perfil del usuario autenticado.
     *
     * @return el perfil del usuario; 401 si no hay token o es inválido
     */
    @GetMapping("/me")
    @Operation(summary = "Obtener el perfil del usuario autenticado")
    @ApiResponse(responseCode = "200", description = "Perfil del usuario")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    public ResponseEntity<UsuarioResponseDTO> obtenerPerfil() {
        return ResponseEntity.ok(usuarioService.obtenerPerfilActual());
    }

    /**
     * Edita los datos personales del usuario autenticado.
     *
     * @param dto nuevos datos del perfil, validados
     * @return el perfil actualizado; 400 si los datos son inválidos
     */
    @PutMapping("/me")
    @Operation(summary = "Editar mis datos personales")
    @ApiResponse(responseCode = "200", description = "Perfil actualizado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    public ResponseEntity<UsuarioResponseDTO> actualizarPerfil(@Valid @RequestBody PerfilRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(dto));
    }
}
