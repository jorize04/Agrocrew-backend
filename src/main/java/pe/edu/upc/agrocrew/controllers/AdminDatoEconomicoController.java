package pe.edu.upc.agrocrew.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.agrocrew.dto.DatoEconomicoDTO;
import pe.edu.upc.agrocrew.dto.DatoEconomicoRequestDTO;
import pe.edu.upc.agrocrew.services.DatoEconomicoService;

import java.util.List;

/** Datos económicos referenciales (US32). Todo /api/v1/admin/** es solo para ADMIN en SecurityConfig. */
@RestController
@RequestMapping("/api/v1/admin/datos-economicos")
@RequiredArgsConstructor
@Tag(name = "Administración - Datos económicos", description = "Solo rol ADMIN")
public class AdminDatoEconomicoController {

    private final DatoEconomicoService datoEconomicoService;

    @GetMapping
    @Operation(summary = "Listar datos económicos, con filtro opcional por cultivo y región")
    public ResponseEntity<List<DatoEconomicoDTO>> listar(@RequestParam(required = false) Long cultivoId,
                                                         @RequestParam(required = false) Long departamentoId) {
        return ResponseEntity.ok(datoEconomicoService.listar(cultivoId, departamentoId));
    }

    @PostMapping
    @Operation(summary = "Registrar precio en chacra, rendimiento y costo de un cultivo en una región y periodo")
    @ApiResponse(responseCode = "201", description = "Dato registrado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos, cultivo o región inexistente")
    @ApiResponse(responseCode = "409", description = "Ya existe un dato del mismo cultivo, región y periodo")
    public ResponseEntity<DatoEconomicoDTO> registrar(@Valid @RequestBody DatoEconomicoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(datoEconomicoService.registrar(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un dato económico")
    @ApiResponse(responseCode = "200", description = "Dato actualizado")
    @ApiResponse(responseCode = "404", description = "Dato no encontrado")
    @ApiResponse(responseCode = "409", description = "Ya existe otro dato del mismo cultivo, región y periodo")
    public ResponseEntity<DatoEconomicoDTO> actualizar(@PathVariable Long id,
                                                       @Valid @RequestBody DatoEconomicoRequestDTO dto) {
        return ResponseEntity.ok(datoEconomicoService.actualizar(id, dto));
    }
}
