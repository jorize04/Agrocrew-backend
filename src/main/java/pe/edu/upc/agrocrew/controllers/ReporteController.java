package pe.edu.upc.agrocrew.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.agrocrew.dto.*;
import pe.edu.upc.agrocrew.services.ReporteService;

import java.time.LocalDate;
import java.util.List;

/** Reportes agregados para asesores técnicos y gobiernos locales (segmento 3) y para el administrador. */
@Slf4j
@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ASESOR', 'ADMIN')")
@Tag(name = "Reportes", description = "Indicadores agregados por zona (roles ASESOR y ADMIN)")
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/predios-por-departamento")
    @Operation(summary = "Cantidad de predios registrados por departamento")
    public ResponseEntity<List<ReporteConteoDTO>> prediosPorDepartamento() {
        return ResponseEntity.ok(reporteService.prediosPorDepartamento());
    }

    @GetMapping("/evaluaciones-por-grupo-cum")
    @Operation(summary = "Evaluaciones por grupo de Capacidad de Uso Mayor del suelo")
    public ResponseEntity<List<ReporteConteoDTO>> evaluacionesPorGrupoCum(
            @Parameter(description = "Opcional: id del departamento (ver /api/v1/ubigeo/departamentos)")
            @RequestParam(required = false) Long departamentoId) {
        return ResponseEntity.ok(reporteService.evaluacionesPorGrupoCum(departamentoId));
    }

    @GetMapping("/cultivos-mas-recomendados")
    @Operation(summary = "Top 10 de cultivos más recomendados, con su puntaje promedio",
            description = "Cuenta las veces que cada cultivo quedó en el top 3 de una evaluación. "
                    + "Por defecto considera los últimos 12 meses.")
    public ResponseEntity<List<ReporteCultivoDTO>> cultivosMasRecomendados(
            @RequestParam(required = false) Long departamentoId,
            @Parameter(example = "2026-01-01") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @Parameter(example = "2026-12-31") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(reporteService.cultivosMasRecomendados(departamentoId, desde, hasta));
    }

    @GetMapping("/puntos-criticos-por-departamento")
    @Operation(summary = "Puntos críticos de la ANA por departamento y nivel de riesgo")
    public ResponseEntity<List<ReporteNivelRiesgoDTO>> puntosCriticosPorDepartamento() {
        return ResponseEntity.ok(reporteService.puntosCriticosPorDepartamento());
    }

    @GetMapping("/predios-por-nivel-riesgo")
    @Operation(summary = "Predios evaluados por departamento y nivel de riesgo hídrico")
    public ResponseEntity<List<ReporteNivelRiesgoDTO>> prediosPorNivelRiesgo() {
        return ResponseEntity.ok(reporteService.prediosPorNivelRiesgo());
    }

    @GetMapping("/alertas-por-mes")
    @Operation(summary = "Alertas generadas por mes y tipo en un año")
    public ResponseEntity<List<ReporteMensualDTO>> alertasPorMes(
            @Parameter(example = "2026") @RequestParam int anio) {
        return ResponseEntity.ok(reporteService.alertasPorMes(anio));
    }

    @GetMapping("/indicadores")
    @Operation(summary = "Indicadores generales y métricas de validación de hipótesis")
    public ResponseEntity<IndicadoresDTO> indicadores() {
        return ResponseEntity.ok(reporteService.indicadores());
    }
}
