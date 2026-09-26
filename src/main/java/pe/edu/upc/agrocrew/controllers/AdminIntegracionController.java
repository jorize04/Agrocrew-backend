package pe.edu.upc.agrocrew.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.agrocrew.dto.PuntoCriticoDTO;
import pe.edu.upc.agrocrew.dto.PuntoCriticoRequestDTO;
import pe.edu.upc.agrocrew.dto.RegistroIntegracionDTO;
import pe.edu.upc.agrocrew.dto.SincronizacionDTO;
import pe.edu.upc.agrocrew.services.AlertaService;
import pe.edu.upc.agrocrew.services.PuntoCriticoService;
import pe.edu.upc.agrocrew.services.RegistroIntegracionService;

import java.util.List;
import java.util.Map;

/** Operaciones de administración de las integraciones. Todo /api/v1/admin/** es solo para ADMIN. */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Administración - Integraciones", description = "Solo rol ADMIN")
public class AdminIntegracionController {

    private final PuntoCriticoService puntoCriticoService;
    private final AlertaService alertaService;
    private final RegistroIntegracionService registroIntegracionService;

    @PostMapping("/puntos-criticos/sincronizar")
    @Operation(summary = "Descargar ahora los puntos críticos de la ANA (también corre todos los días a las 3:00)")
    @ApiResponse(responseCode = "200", description = "Resumen de la sincronización")
    @ApiResponse(responseCode = "503", description = "El servicio de la ANA no respondió")
    public ResponseEntity<SincronizacionDTO> sincronizar() {
        return ResponseEntity.ok(puntoCriticoService.sincronizarConAna());
    }

    @PostMapping("/puntos-criticos")
    @Operation(summary = "Registrar un punto crítico manualmente (pruebas o contingencia)")
    @ApiResponse(responseCode = "201", description = "Punto crítico registrado; se generan alertas para predios cercanos")
    @ApiResponse(responseCode = "409", description = "Ya existe un punto con ese código")
    public ResponseEntity<PuntoCriticoDTO> registrar(@Valid @RequestBody PuntoCriticoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(puntoCriticoService.registrarManual(dto));
    }

    @PostMapping("/alertas/revisar-lluvias")
    @Operation(summary = "Revisar ahora el pronóstico de lluvias de todos los predios (también corre a las 6:00)")
    public ResponseEntity<Map<String, Integer>> revisarLluvias() {
        return ResponseEntity.ok(Map.of("alertasCreadas", alertaService.revisarLluviasIntensas()));
    }

    @GetMapping("/integraciones")
    @Operation(summary = "Últimas 50 llamadas a servicios externos, con su resultado y duración")
    public ResponseEntity<List<RegistroIntegracionDTO>> registros() {
        return ResponseEntity.ok(registroIntegracionService.listarRecientes().stream()
                .map(r -> new RegistroIntegracionDTO(r.getId(), r.getServicio(), r.getEndpoint(), r.getEstadoHttp(),
                        r.getDuracionMs(), r.getExito(), r.getMensajeError(), r.getFecha()))
                .toList());
    }
}
