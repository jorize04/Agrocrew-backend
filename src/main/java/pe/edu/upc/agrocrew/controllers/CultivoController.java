package pe.edu.upc.agrocrew.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.agrocrew.dto.CultivoResponseDTO;
import pe.edu.upc.agrocrew.dto.GrupoCumDTO;
import pe.edu.upc.agrocrew.services.CultivoService;

import java.util.List;

/** Consulta del catálogo para cualquier usuario autenticado. */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Catálogo", description = "Cultivos y grupos de Capacidad de Uso Mayor (consulta)")
public class CultivoController {

    private final CultivoService cultivoService;

    @GetMapping("/cultivos")
    @Operation(summary = "Listar cultivos activos")
    public ResponseEntity<List<CultivoResponseDTO>> listar() {
        return ResponseEntity.ok(cultivoService.listarActivos());
    }

    @GetMapping("/cultivos/{id}")
    @Operation(summary = "Obtener un cultivo con sus requerimientos")
    @ApiResponse(responseCode = "200", description = "Cultivo encontrado")
    @ApiResponse(responseCode = "404", description = "Cultivo no encontrado")
    public ResponseEntity<CultivoResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(cultivoService.obtener(id));
    }

    @GetMapping("/grupos-cum")
    @Operation(summary = "Listar grupos de Capacidad de Uso Mayor con su explicación sencilla")
    public ResponseEntity<List<GrupoCumDTO>> listarGruposCum() {
        return ResponseEntity.ok(cultivoService.listarGruposCum());
    }
}
