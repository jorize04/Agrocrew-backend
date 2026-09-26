package pe.edu.upc.agrocrew.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.agrocrew.dto.UbigeoDTO;
import pe.edu.upc.agrocrew.services.UbigeoService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ubigeo")
@RequiredArgsConstructor
@Tag(name = "Ubigeo", description = "Departamentos, provincias y distritos del Perú (para los combos del formulario)")
public class UbigeoController {

    private final UbigeoService ubigeoService;

    @GetMapping("/departamentos")
    @Operation(summary = "Listar departamentos")
    public ResponseEntity<List<UbigeoDTO>> listarDepartamentos() {
        return ResponseEntity.ok(ubigeoService.listarDepartamentos());
    }

    @GetMapping("/departamentos/{departamentoId}/provincias")
    @Operation(summary = "Listar provincias de un departamento")
    public ResponseEntity<List<UbigeoDTO>> listarProvincias(@PathVariable Long departamentoId) {
        return ResponseEntity.ok(ubigeoService.listarProvincias(departamentoId));
    }

    @GetMapping("/provincias/{provinciaId}/distritos")
    @Operation(summary = "Listar distritos de una provincia")
    public ResponseEntity<List<UbigeoDTO>> listarDistritos(@PathVariable Long provinciaId) {
        return ResponseEntity.ok(ubigeoService.listarDistritos(provinciaId));
    }
}
