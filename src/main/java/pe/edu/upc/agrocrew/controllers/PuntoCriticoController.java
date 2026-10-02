package pe.edu.upc.agrocrew.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
    @ApiResponse(responseCode = "200", description = "Puntos dentro del radio, del más cercano al más lejano (lista vacía si no hay)")
    @ApiResponse(responseCode = "400", description = "Coordenadas fuera de rango o radio fuera de 1 a 50 km")
    public ResponseEntity<List<PuntoCriticoDTO>> cercanos(
            @Parameter(description = "Latitud (-90 a 90)", example = "-12.0681")
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") double latitud,
            @Parameter(description = "Longitud (-180 a 180)", example = "-75.2102")
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double longitud,
            @Parameter(description = "Radio en km (1 a 50)")
            @RequestParam(defaultValue = "10") @DecimalMin("1.0") @DecimalMax("50.0") double radioKm) {
        return ResponseEntity.ok(riesgoService.listarCercanos(latitud, longitud, radioKm));
    }
}