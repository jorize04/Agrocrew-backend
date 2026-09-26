package pe.edu.upc.agrocrew.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.agrocrew.dto.CambioRolRequestDTO;
import pe.edu.upc.agrocrew.dto.UsuarioResponseDTO;
import pe.edu.upc.agrocrew.services.UsuarioService;

import java.util.List;

/** Gestión de usuarios (US33). Todo /api/v1/admin/** es solo para ADMIN. */
@RestController
@RequestMapping("/api/v1/admin/usuarios")
@RequiredArgsConstructor
@Tag(name = "Administración - Usuarios", description = "Solo rol ADMIN")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @Operation(summary = "Listar todos los usuarios")
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @PatchMapping("/{id}/rol")
    @Operation(summary = "Cambiar el rol de un usuario")
    @ApiResponse(responseCode = "200", description = "Rol actualizado")
    @ApiResponse(responseCode = "400", description = "Rol inválido o intento de cambiar el propio rol")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    public ResponseEntity<UsuarioResponseDTO> cambiarRol(@PathVariable Long id,
                                                         @Valid @RequestBody CambioRolRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.cambiarRol(id, dto.getRol()));
    }

    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar una cuenta (ya no podrá iniciar sesión)")
    public ResponseEntity<UsuarioResponseDTO> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.cambiarEstado(id, false));
    }

    @PatchMapping("/{id}/activar")
    @Operation(summary = "Volver a activar una cuenta")
    public ResponseEntity<UsuarioResponseDTO> activar(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.cambiarEstado(id, true));
    }
}
