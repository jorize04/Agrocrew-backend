package pe.edu.upc.agrocrew.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.agrocrew.dto.EvaluacionRequestDTO;
import pe.edu.upc.agrocrew.dto.EvaluacionResponseDTO;
import pe.edu.upc.agrocrew.dto.EvaluacionResumenDTO;
import pe.edu.upc.agrocrew.services.EvaluacionService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PRODUCTOR')")
@Tag(name = "Evaluaciones", description = "Ranking de cultivos compatibles con el predio")
public class EvaluacionController {

    private final EvaluacionService evaluacionService;

    @PostMapping("/predios/{predioId}/evaluaciones")
    @Operation(summary = "Evaluar el predio y obtener el ranking de cultivos",
            description = "El cuerpo es opcional. Envíe {\"cultivoId\": N} para evaluar además un cultivo específico.")
    @ApiResponse(responseCode = "201", description = "Evaluación creada (estado COMPLETADA o PARCIAL)")
    @ApiResponse(responseCode = "404", description = "Predio o cultivo no encontrado")
    public ResponseEntity<EvaluacionResponseDTO> evaluar(@PathVariable Long predioId,
                                                         @RequestBody(required = false) EvaluacionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(evaluacionService.evaluar(predioId, dto));
    }

    @GetMapping("/predios/{predioId}/evaluaciones")
    @Operation(summary = "Historial de evaluaciones del predio (paginado)")
    public ResponseEntity<Page<EvaluacionResumenDTO>> historial(@PathVariable Long predioId,
                                                                @RequestParam(defaultValue = "0") int pagina,
                                                                @RequestParam(defaultValue = "10") int tamano) {
        return ResponseEntity.ok(evaluacionService.historial(predioId, pagina, tamano));
    }

    @GetMapping("/evaluaciones/{id}")
    @Operation(summary = "Detalle de una evaluación")
    @ApiResponse(responseCode = "404", description = "No existe o no pertenece al usuario")
    public ResponseEntity<EvaluacionResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(evaluacionService.obtener(id));
    }
}
