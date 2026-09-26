package pe.edu.upc.agrocrew.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.agrocrew.dto.AlertaDTO;
import pe.edu.upc.agrocrew.services.AlertaService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/alertas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PRODUCTOR')")
@Tag(name = "Alertas", description = "Alertas de riesgo hídrico y lluvia intensa de mis predios")
public class AlertaController {

    private final AlertaService alertaService;

    @GetMapping
    @Operation(summary = "Listar mis alertas, de la más reciente a la más antigua")
    public ResponseEntity<List<AlertaDTO>> listar(
            @Parameter(description = "true = solo leídas, false = solo no leídas, vacío = todas")
            @RequestParam(required = false) Boolean leida) {
        return ResponseEntity.ok(alertaService.listarMisAlertas(leida));
    }

    @GetMapping("/no-leidas/cantidad")
    @Operation(summary = "Cantidad de alertas sin leer (para el indicador de la app)")
    public ResponseEntity<Map<String, Long>> cantidadNoLeidas() {
        return ResponseEntity.ok(Map.of("noLeidas", alertaService.contarNoLeidas()));
    }

    @PatchMapping("/{id}/leida")
    @Operation(summary = "Marcar una alerta como leída")
    @ApiResponse(responseCode = "204", description = "Alerta marcada como leída")
    @ApiResponse(responseCode = "404", description = "No existe o no pertenece al usuario")
    public ResponseEntity<Void> marcarLeida(@PathVariable Long id) {
        alertaService.marcarComoLeida(id);
        return ResponseEntity.noContent().build();
    }
}
