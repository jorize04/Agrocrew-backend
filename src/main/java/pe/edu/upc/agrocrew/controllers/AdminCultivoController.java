package pe.edu.upc.agrocrew.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.agrocrew.dto.CultivoRequestDTO;
import pe.edu.upc.agrocrew.dto.CultivoResponseDTO;
import pe.edu.upc.agrocrew.services.CultivoService;

import java.util.List;

/** Mantenimiento del catálogo. Todo /api/v1/admin/** está restringido al rol ADMIN en SecurityConfig. */
@RestController
@RequestMapping("/api/v1/admin/cultivos")
@RequiredArgsConstructor
@Tag(name = "Administración - Cultivos", description = "Solo rol ADMIN")
public class AdminCultivoController {

    private final CultivoService cultivoService;

    @GetMapping
    @Operation(summary = "Listar todos los cultivo, incluidos los desactivados")
    public ResponseEntity<List<CultivoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(cultivoService.listarTodos());
    }

    @PostMapping
    @Operation(summary = "Registrar un cultivo con sus requerimientos")
    @ApiResponse(responseCode = "201", description = "Cultivo registrado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos, rango mínimo mayor al máximo o grupo CUM inválido")
    @ApiResponse(responseCode = "409", description = "Ya existe un cultivo con ese nombre")
    public ResponseEntity<CultivoResponseDTO> registrar(@Valid @RequestBody CultivoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cultivoService.registrar(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar un cultivo y sus requerimientos")
    @ApiResponse(responseCode = "200", description = "Cultivo actualizado")
    @ApiResponse(responseCode = "404", description = "Cultivo no encontrado")
    @ApiResponse(responseCode = "409", description = "Ya existe otro cultivo con ese nombre")
    public ResponseEntity<CultivoResponseDTO> actualizar(@PathVariable Long id,
                                                         @Valid @RequestBody CultivoRequestDTO dto) {
        return ResponseEntity.ok(cultivoService.actualizar(id, dto));
    }

    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar un cultivo (deja de recomendarse)")
    @ApiResponse(responseCode = "204", description = "Cultivo desactivado")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        cultivoService.cambiarEstado(id, false);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activar")
    @Operation(summary = "Volver a activar un cultivo")
    @ApiResponse(responseCode = "204", description = "Cultivo activado")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        cultivoService.cambiarEstado(id, true);
        return ResponseEntity.noContent().build();
    }
}
