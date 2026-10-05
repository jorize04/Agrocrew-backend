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

/**
 * CRUD de los predios (terrenos) del productor autenticado.
 *
 * <p>Un productor solo puede ver y modificar sus propios predios. Solo es accesible para el
 * rol {@code PRODUCTOR}.</p>
 */
@RestController
@RequestMapping("/api/v1/predios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PRODUCTOR')")
@Tag(name = "Predios", description = "Terrenos del productor autenticado")
public class PredioController {

    private final PredioService predioService;

    /**
     * Registra un predio nuevo.
     *
     * @param dto datos del predio validados
     * @return el predio creado con código 201; 400 si los datos son inválidos, la ubicación está
     *         fuera del Perú o el distrito no existe
     */
    @PostMapping
    @Operation(summary = "Registrar un predio")
    @ApiResponse(responseCode = "201", description = "Predio registrado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos, ubicación fuera del Perú o distrito inexistente")
    public ResponseEntity<PredioResponseDTO> registrar(@Valid @RequestBody PredioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(predioService.registrar(dto));
    }

    /**
     * Lista los predios del usuario autenticado.
     *
     * @return lista de predios, vacía si no tiene ninguno
     */
    @GetMapping
    @Operation(summary = "Listar mis predios")
    @ApiResponse(responseCode = "200", description = "Lista de predios (vacía si no tiene)")
    public ResponseEntity<List<PredioResponseDTO>> listar() {
        return ResponseEntity.ok(predioService.listarMisPredios());
    }

    /**
     * Obtiene un predio del usuario.
     *
     * @param id identificador del predio
     * @return el predio; 404 si no existe o no pertenece al usuario
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener un predio")
    @ApiResponse(responseCode = "200", description = "Predio encontrado")
    @ApiResponse(responseCode = "404", description = "No existe o no pertenece al usuario")
    public ResponseEntity<PredioResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(predioService.obtener(id));
    }

    /**
     * Actualiza los datos de un predio.
     *
     * @param id  identificador del predio
     * @param dto nuevos datos del predio, validados
     * @return el predio actualizado; 400 si los datos son inválidos, 404 si no existe o
     *         no pertenece al usuario
     */
    @PutMapping("/{id}")
    @Operation(summary = "Editar un predio")
    @ApiResponse(responseCode = "200", description = "Predio actualizado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "No existe o no pertenece al usuario")
    public ResponseEntity<PredioResponseDTO> actualizar(@PathVariable Long id,
                                                        @Valid @RequestBody PredioRequestDTO dto) {
        return ResponseEntity.ok(predioService.actualizar(id, dto));
    }

    /**
     * Elimina un predio.
     *
     * @param id identificador del predio
     * @return respuesta 204 sin contenido; 404 si no existe o no pertenece al usuario
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un predio")
    @ApiResponse(responseCode = "204", description = "Predio eliminado")
    @ApiResponse(responseCode = "404", description = "No existe o no pertenece al usuario")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        predioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
