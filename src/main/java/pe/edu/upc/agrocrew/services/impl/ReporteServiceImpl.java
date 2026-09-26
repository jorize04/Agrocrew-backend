package pe.edu.upc.agrocrew.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agrocrew.dto.*;
import pe.edu.upc.agrocrew.exceptions.ReglaNegocioException;
import pe.edu.upc.agrocrew.models.Compatibilidad;
import pe.edu.upc.agrocrew.models.EstadoEvaluacion;
import pe.edu.upc.agrocrew.models.Rol;
import pe.edu.upc.agrocrew.repositories.*;
import pe.edu.upc.agrocrew.services.ReporteService;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {

    private static final int TOP_CULTIVOS = 10;

    private final PredioRepository predioRepository;
    private final EvaluacionRepository evaluacionRepository;
    private final RecomendacionRepository recomendacionRepository;
    private final PuntoCriticoRepository puntoCriticoRepository;
    private final AlertaRepository alertaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CultivoRepository cultivoRepository;

    @Override
    public List<ReporteConteoDTO> prediosPorDepartamento() {
        log.info("Reporte: predios por departamento");
        return predioRepository.reportePrediosPorDepartamento();
    }

    @Override
    public List<ReporteConteoDTO> evaluacionesPorGrupoCum(Long departamentoId) {
        log.info("Reporte: evaluaciones por grupo CUM (departamento={})", departamentoId);
        return evaluacionRepository.reporteEvaluacionesPorGrupoCum(departamentoId);
    }

    @Override
    public List<ReporteCultivoDTO> cultivosMasRecomendados(Long departamentoId, LocalDate desde, LocalDate hasta) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.minusYears(1);
        if (inicio.isAfter(fin)) {
            throw new ReglaNegocioException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        log.info("Reporte: cultivos más recomendados ({} a {}, departamento={})", inicio, fin, departamentoId);
        return recomendacionRepository.reporteCultivosMasRecomendados(Compatibilidad.BAJA,
                inicio.atStartOfDay(), fin.plusDays(1).atStartOfDay(), departamentoId,
                PageRequest.of(0, TOP_CULTIVOS));
    }

    @Override
    public List<ReporteNivelRiesgoDTO> puntosCriticosPorDepartamento() {
        log.info("Reporte: puntos críticos por departamento");
        return puntoCriticoRepository.reportePuntosCriticosPorDepartamento().stream()
                .map(fila -> new ReporteNivelRiesgoDTO((String) fila[0], (String) fila[1],
                        ((Number) fila[2]).longValue()))
                .toList();
    }

    @Override
    public List<ReporteNivelRiesgoDTO> prediosPorNivelRiesgo() {
        log.info("Reporte: predios por nivel de riesgo hídrico");
        return evaluacionRepository.reportePrediosPorNivelRiesgo();
    }

    @Override
    public List<ReporteMensualDTO> alertasPorMes(int anio) {
        if (anio < 2020 || anio > LocalDate.now().getYear() + 1) {
            throw new ReglaNegocioException("El año debe estar entre 2020 y " + (LocalDate.now().getYear() + 1));
        }
        log.info("Reporte: alertas por mes del año {}", anio);
        return alertaRepository.reporteAlertasPorMes(anio).stream()
                .map(fila -> new ReporteMensualDTO(((Number) fila[0]).intValue(), (String) fila[1],
                        ((Number) fila[2]).longValue()))
                .toList();
    }

    @Override
    public IndicadoresDTO indicadores() {
        log.info("Reporte: indicadores generales");
        long evaluaciones = evaluacionRepository.count();
        return new IndicadoresDTO(
                usuarioRepository.countByRolNombre(Rol.PRODUCTOR),
                usuarioRepository.countByRolNombre(Rol.ASESOR),
                predioRepository.countByActivoTrue(),
                evaluaciones,
                porcentaje(evaluacionRepository.countByEstado(EstadoEvaluacion.COMPLETADA), evaluaciones),
                porcentaje(evaluacionRepository.countByAlertaActivaFalse(), evaluaciones),
                alertaRepository.count(),
                alertaRepository.countByLeidaFalse(),
                puntoCriticoRepository.count(),
                cultivoRepository.countByActivoTrue());
    }

    private double porcentaje(long parte, long total) {
        return total == 0 ? 0 : Math.round(parte * 1000.0 / total) / 10.0;
    }
}
