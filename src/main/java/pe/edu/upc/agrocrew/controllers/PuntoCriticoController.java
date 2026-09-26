package pe.edu.upc.agrocrew.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.agrocrew.dto.PuntoCriticoDTO;
import pe.edu.upc.agrocrew.services.RiesgoService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/puntos-criticos")
@RequiredArgsConstructor
@Tag(name = "Puntos críticos", description = "Puntos críticos de la ANA para el mapa de riesgo")
public class PuntoCriticoController {

    private final RiesgoService riesgoService;

    @GetMapping
    @Operation(summary = "Puntos críticos alrededor de una coordenada, ordenados por distancia")
    public ResponseEntity<List<PuntoCriticoDTO>> cercanos(
            @Parameter(example = "-12.0681") @RequestParam double latitud,
            @Parameter(example = "-75.2102") @RequestParam double longitud,
            @Parameter(description = "Radio en km (1 a 50)") @RequestParam(defaultValue = "10") double radioKm) {
        return ResponseEntity.ok(riesgoService.listarCercanos(latitud, longitud, radioKm));
    }
}
