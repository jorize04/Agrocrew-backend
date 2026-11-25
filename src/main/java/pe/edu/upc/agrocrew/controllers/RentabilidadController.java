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
import pe.edu.upc.agrocrew.dto.RentabilidadDTO;
import pe.edu.upc.agrocrew.services.DatoEconomicoService;

/**
 * Rentabilidad referencial de un cultivo en la región del predio (US22).
 */
@RestController
@RequestMapping("/api/v1/predios/{predioId}/cultivos/{cultivoId}/rentabilidad")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PRODUCTOR')")
@Tag(name = "Información del predio", description = "Suelo (MIDAGRI), clima (Open-Meteo) y riesgo hídrico (ANA)")
public class RentabilidadController {

    private final DatoEconomicoService datoEconomicoService;

    @GetMapping
    @Operation(summary = "Ingreso, costo y margen referenciales por hectárea del cultivo en la región del predio",
            description = "Usa el dato más reciente del cultivo en el departamento del predio. "
                    + "Si no hay datos, responde disponible = false y sin cifras.")
    @ApiResponse(responseCode = "200", description = "Rentabilidad (o aviso de que no hay datos)")
    @ApiResponse(responseCode = "404", description = "Predio o cultivo no encontrado")
    public ResponseEntity<RentabilidadDTO> rentabilidad(@PathVariable Long predioId, @PathVariable Long cultivoId) {
        return ResponseEntity.ok(datoEconomicoService.rentabilidad(predioId, cultivoId));
    }
}
