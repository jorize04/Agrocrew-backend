package pe.edu.upc.agrocrew.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.agrocrew.dto.ClimaDTO;
import pe.edu.upc.agrocrew.dto.RiesgoDTO;
import pe.edu.upc.agrocrew.dto.SueloDTO;
import pe.edu.upc.agrocrew.services.ClimaService;
import pe.edu.upc.agrocrew.services.RiesgoService;
import pe.edu.upc.agrocrew.services.SueloService;

@RestController
@RequestMapping("/api/v1/predios/{predioId}")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PRODUCTOR')")
@Tag(name = "Información del predio", description = "Suelo (MIDAGRI), clima (Open-Meteo) y riesgo hídrico (ANA)")
public class ConsultaPredioController {

    private final SueloService sueloService;
    private final ClimaService climaService;
    private final RiesgoService riesgoService;

    @GetMapping("/suelo")
    @Operation(summary = "Capacidad de Uso Mayor del suelo del predio, con explicación sencilla")
    @ApiResponse(responseCode = "200", description = "Clasificación encontrada, o disponible=false si no hay dato oficial")
    @ApiResponse(responseCode = "503", description = "El servicio del MIDAGRI no respondió")
    public ResponseEntity<SueloDTO> suelo(@PathVariable Long predioId) {
        return ResponseEntity.ok(sueloService.consultarSueloDePredio(predioId));
    }

    @GetMapping("/clima")
    @Operation(summary = "Clima de los últimos 12 meses y pronóstico de 7 días")
    @ApiResponse(responseCode = "200", description = "Clima actual o, si Open-Meteo falla, el último guardado (datosGuardados=true)")
    @ApiResponse(responseCode = "503", description = "Open-Meteo no respondió y no hay datos guardados")
    public ResponseEntity<ClimaDTO> clima(@PathVariable Long predioId) {
        return ResponseEntity.ok(climaService.obtenerClimaDePredio(predioId));
    }

    @GetMapping("/riesgo")
    @Operation(summary = "Nivel de riesgo hídrico según puntos críticos de la ANA cercanos")
    public ResponseEntity<RiesgoDTO> riesgo(@PathVariable Long predioId) {
        return ResponseEntity.ok(riesgoService.consultarRiesgoDePredio(predioId));
    }
}
