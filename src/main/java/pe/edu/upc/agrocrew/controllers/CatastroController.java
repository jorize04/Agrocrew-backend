package pe.edu.upc.agrocrew.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.agrocrew.dto.CatastroDTO;
import pe.edu.upc.agrocrew.services.CatastroService;

/**
 * Búsqueda en el Catastro Rural del MIDAGRI para registrar el predio por su código catastral (US07).
 */
@RestController
@RequestMapping("/api/v1/catastro")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PRODUCTOR')")
@Tag(name = "Predios", description = "Terrenos del productor autenticado")
public class CatastroController {

    private final CatastroService catastroService;

    @GetMapping("/{codigo}")
    @Operation(summary = "Buscar un predio en el Catastro Rural por su código catastral",
            description = "El catastro se publica por departamento, por eso se indica el departamento del predio. "
                    + "Devuelve la ubicación (centro del predio) y el área oficiales para precargar el registro.")
    @ApiResponse(responseCode = "200", description = "Predio encontrado")
    @ApiResponse(responseCode = "400", description = "Código con caracteres no permitidos o departamento inválido")
    @ApiResponse(responseCode = "404", description = "El código no existe en el catastro de ese departamento")
    @ApiResponse(responseCode = "503", description = "El servicio del MIDAGRI no está disponible")
    public ResponseEntity<CatastroDTO> buscar(
            @Parameter(example = "706980") @PathVariable String codigo,
            @Parameter(example = "12", description = "Id del departamento (ver GET /api/v1/ubigeo/departamentos)")
            @RequestParam Long departamentoId) {
        return ResponseEntity.ok(catastroService.buscar(codigo, departamentoId));
    }
}
