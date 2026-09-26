package pe.edu.upc.agrocrew.services;

import pe.edu.upc.agrocrew.dto.*;

import java.time.LocalDate;
import java.util.List;

/** Reportes para asesores técnicos, gobiernos locales y administradores (US26 a US28, US34). */
public interface ReporteService {

    List<ReporteConteoDTO> prediosPorDepartamento();

    List<ReporteConteoDTO> evaluacionesPorGrupoCum(Long departamentoId);

    List<ReporteCultivoDTO> cultivosMasRecomendados(Long departamentoId, LocalDate desde, LocalDate hasta);

    List<ReporteNivelRiesgoDTO> puntosCriticosPorDepartamento();

    List<ReporteNivelRiesgoDTO> prediosPorNivelRiesgo();

    List<ReporteMensualDTO> alertasPorMes(int anio);

    IndicadoresDTO indicadores();
}
