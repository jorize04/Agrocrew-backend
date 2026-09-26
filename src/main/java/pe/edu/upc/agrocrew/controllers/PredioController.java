package pe.edu.upc.agrocrew.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.agrocrew.dto.PredioRequestDTO;
import pe.edu.upc.agrocrew.dto.PredioResponseDTO;
import pe.edu.upc.agrocrew.services.PredioService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/predios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PRODUCTOR')")
@Tag(name = "Predios", description = "Terrenos del productor autenticado")
public class PredioController {

    private final PredioService predioService;

    @PostMapping
    @Operation(summary = "Registrar un predio")
    @ApiResponse(responseCode = "201", description = "Predio registrado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos, ubicación fuera del Perú o distrito inexistente")
    public ResponseEntity<PredioResponseDTO> registrar(@Valid @RequestBody PredioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(predioService.registrar(dto));
    }

    @GetMapping
    @Operation(summary = "Listar mis predios")
    @ApiResponse(responseCode = "200", description = "Lista de predios (vacía si no tiene)")
    public ResponseEntity<List<PredioResponseDTO>> listar() {
        return ResponseEntity.ok(predioService.listarMisPredios());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un predio")
    @ApiResponse(responseCode = "200", description = "Predio encontrado")
    @ApiResponse(responseCode = "404", description = "No existe o no pertenece al usuario")
    public ResponseEntity<PredioResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(predioService.obtener(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar un predio")
    @ApiResponse(responseCode = "200", description = "Predio actualizado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "No existe o no pertenece al usuario")
    public ResponseEntity<PredioResponseDTO> actualizar(@PathVariable Long id,
                                                        @Valid @RequestBody PredioRequestDTO dto) {
        return ResponseEntity.ok(predioService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un predio")
    @ApiResponse(responseCode = "204", description = "Predio eliminado")
    @ApiResponse(responseCode = "404", description = "No existe o no pertenece al usuario")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        predioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
