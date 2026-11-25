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
import pe.edu.upc.agrocrew.dto.CalificacionDTO;
import pe.edu.upc.agrocrew.dto.CalificacionRequestDTO;
import pe.edu.upc.agrocrew.services.CalificacionService;

/**
 * Calificación de utilidad de una evaluación (US25).
 */
@RestController
@RequestMapping("/api/v1/evaluaciones/{evaluacionId}/calificacion")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PRODUCTOR')")
@Tag(name = "Evaluaciones", description = "Ranking de cultivos compatibles con el predio")
public class CalificacionController {

    private final CalificacionService calificacionService;

    @PostMapping
    @Operation(summary = "Calificar la utilidad de la evaluación (1 a 5); si ya la calificó, se actualiza")
    @ApiResponse(responseCode = "201", description = "Calificación registrada")
    @ApiResponse(responseCode = "200", description = "Calificación anterior actualizada")
    @ApiResponse(responseCode = "400", description = "Calificación fuera de 1 a 5 o comentario muy largo")
    @ApiResponse(responseCode = "404", description = "Evaluación no encontrada")
    public ResponseEntity<CalificacionDTO> calificar(@PathVariable Long evaluacionId,
                                                     @Valid @RequestBody CalificacionRequestDTO dto) {
        CalificacionDTO resultado = calificacionService.calificar(evaluacionId, dto);
        return ResponseEntity.status(resultado.actualizada() ? HttpStatus.OK : HttpStatus.CREATED).body(resultado);
    }

    @GetMapping
    @Operation(summary = "Ver mi calificación de la evaluación")
    @ApiResponse(responseCode = "200", description = "Calificación actual")
    @ApiResponse(responseCode = "204", description = "La evaluación aún no tiene calificación")
    @ApiResponse(responseCode = "404", description = "Evaluación no encontrada")
    public ResponseEntity<CalificacionDTO> obtener(@PathVariable Long evaluacionId) {
        return calificacionService.obtener(evaluacionId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
