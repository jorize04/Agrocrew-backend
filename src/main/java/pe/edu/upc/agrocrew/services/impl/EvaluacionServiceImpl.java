package pe.edu.upc.agrocrew.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agrocrew.dto.*;
import pe.edu.upc.agrocrew.exceptions.IntegracionException;
import pe.edu.upc.agrocrew.exceptions.RecursoNoEncontradoException;
import pe.edu.upc.agrocrew.exceptions.ReglaNegocioException;
import pe.edu.upc.agrocrew.integraciones.MidagriClient;
import pe.edu.upc.agrocrew.integraciones.OpenMeteoClient;
import pe.edu.upc.agrocrew.models.*;
import pe.edu.upc.agrocrew.repositories.AlertaRepository;
import pe.edu.upc.agrocrew.repositories.CultivoRepository;
import pe.edu.upc.agrocrew.repositories.EvaluacionRepository;
import pe.edu.upc.agrocrew.repositories.GrupoCumRepository;
import pe.edu.upc.agrocrew.services.*;
import pe.edu.upc.agrocrew.util.CumParser;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluacionServiceImpl implements EvaluacionService {

    public static final String AVISO = "Esta recomendación es referencial. No reemplaza la evaluación de un "
            + "ingeniero agrónomo ni garantiza el rendimiento de la cosecha.";
    private static final int MAX_RANKING = 10;

    private final PredioService predioService;
    private final UsuarioService usuarioService;
    private final MidagriClient midagriClient;
    private final ClimaService climaService;
    private final BuscadorPuntosCriticos buscador;
    private final MotorReglasService motor;
    private final CultivoRepository cultivoRepository;
    private final GrupoCumRepository grupoCumRepository;
    private final EvaluacionRepository evaluacionRepository;
    private final AlertaRepository alertaRepository;
    private final ExplicacionIaService explicacionIaService;

    @Value("${app.riesgo.radio-km:5}")
    private double radioKm;

    @Override
    @Transactional
    public EvaluacionResponseDTO evaluar(Long predioId, EvaluacionRequestDTO dto) {
        Predio predio = predioService.obtenerPredioDelUsuarioActual(predioId);
        Cultivo consultado = null;
        if (dto != null && dto.getCultivoId() != null) {
            consultado = cultivoRepository.findById(dto.getCultivoId())
                    .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                    .orElseThrow(() -> new RecursoNoEncontradoException("Cultivo no encontrado"));
        }
        List<String> faltantes = new ArrayList<>();

        // 1) Suelo (MIDAGRI). Si no responde, se sigue sin este dato.
        Optional<CumParser.Cum> cum = Optional.empty();
        try {
            cum = midagriClient.consultarCum(predio.getLatitud(), predio.getLongitud());
        } catch (IntegracionException e) {
            faltantes.add(ServicioExterno.MIDAGRI.name());
        }
        GrupoCum grupo = cum.flatMap(c -> grupoCumRepository.findByCodigo(c.grupo())).orElse(null);

        // 2) Clima (Open-Meteo, con respaldo del último dato guardado).
        OpenMeteoClient.ClimaAnual clima = null;
        try {
            clima = climaService.obtenerClimaAnual(predio);
        } catch (IntegracionException e) {
            faltantes.add(ServicioExterno.OPEN_METEO.name());
        }

        // 3) Riesgo hídrico (puntos críticos ya sincronizados en la base de datos).
        List<BuscadorPuntosCriticos.PuntoCercano> cercanos =
                buscador.buscar(predio.getLatitud(), predio.getLongitud(), radioKm);
        NivelRiesgo nivelRiesgo = cercanos.stream().map(pc -> pc.punto().getNivelRiesgo())
                .max(Comparator.naturalOrder()).orElse(NivelRiesgo.BAJO);

        // 4) Motor de reglas.
        MotorReglasService.DatosPredio datos = new MotorReglasService.DatosPredio(
                grupo != null ? grupo.getCodigo() : null,
                predio.getAltitudMsnm(),
                clima != null ? clima.temperaturaMedia() : null,
                clima != null ? clima.precipitacionAnualMm() : null,
                predio.getFuenteAgua(),
                nivelRiesgo);
        List<MotorReglasService.ResultadoCultivo> resultados =
                motor.evaluar(datos, cultivoRepository.findActivosParaMotor());
        if (resultados.isEmpty()) {
            throw new ReglaNegocioException("El catálogo no tiene cultivos activos para evaluar");
        }

        // 5) Guardar la evaluación con la foto de los datos usados.
        Evaluacion ev = new Evaluacion();
        ev.setPredio(predio);
        ev.setTipo(consultado != null ? TipoEvaluacion.CULTIVO_ESPECIFICO : TipoEvaluacion.GENERAL);
        ev.setCultivoConsultado(consultado);
        ev.setEstado(faltantes.isEmpty() ? EstadoEvaluacion.COMPLETADA : EstadoEvaluacion.PARCIAL);
        ev.setFuentesFaltantes(faltantes.isEmpty() ? null : String.join(",", faltantes));
        ev.setGrupoCum(grupo);
        cum.ifPresent(c -> {
            ev.setCumCodigoOriginal(recortar(c.codigoOriginal(), 40));
            ev.setCalidadAgrologica(c.calidad());
            ev.setLimitacionesSuelo(recortar(c.limitaciones(), 100));
        });
        ev.setAltitudMsnm(predio.getAltitudMsnm());
        ev.setTemperaturaMedia(datos.temperaturaMedia());
        ev.setPrecipitacionAnualMm(datos.precipitacionAnualMm());
        ev.setFuenteAgua(predio.getFuenteAgua());
        ev.setNivelRiesgoHidrico(nivelRiesgo);
        if (!cercanos.isEmpty()) {
            ev.setPuntoCritico(cercanos.get(0).punto());
            ev.setDistanciaPuntoCriticoKm(cercanos.get(0).distanciaKm());
        }
        ev.setAlertaActiva(alertaRepository.existsByPredioIdAndLeidaFalse(predio.getId()));

        List<MotorReglasService.ResultadoCultivo> ranking = resultados.stream()
                .filter(r -> r.compatibilidad() != Compatibilidad.BAJA)
                .limit(MAX_RANKING)
                .toList();
        for (int i = 0; i < ranking.size(); i++) {
            ev.agregarRecomendacion(crearRecomendacion(ranking.get(i), i + 1));
        }
        MotorReglasService.ResultadoCultivo resultadoConsultado = null;
        if (consultado != null) {
            Long idConsultado = consultado.getId();
            for (int i = 0; i < resultados.size(); i++) {
                if (resultados.get(i).cultivo().getId().equals(idConsultado)) {
                    resultadoConsultado = resultados.get(i);
                    if (!ranking.contains(resultadoConsultado)) {
                        ev.agregarRecomendacion(crearRecomendacion(resultadoConsultado, i + 1));
                    }
                    break;
                }
            }
        }
        // 6) Explicación: primero con IA; si no está configurada o falla, con plantilla.
        Optional<ExplicacionIaService.Resultado> ia =
                explicacionIaService.generar(predio, grupo, datos, ranking, resultadoConsultado);
        if (ia.isPresent()) {
            ev.setExplicacion(ia.get().texto());
            ev.setExplicacionPorIa(true);
            ev.setModeloIa(recortar(ia.get().modelo(), 60));
        } else {
            ev.setExplicacion(generarExplicacion(predio, grupo, datos, cercanos, ranking, resultadoConsultado, faltantes));
            ev.setExplicacionPorIa(false);
        }

        Evaluacion guardada = evaluacionRepository.save(ev);
        log.info("Evaluación {} del predio {}: estado={}, cultivos compatibles={}",
                guardada.getId(), predio.getId(), guardada.getEstado(), ranking.size());
        return convertir(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EvaluacionResumenDTO> historial(Long predioId, int pagina, int tamano) {
        Predio predio = predioService.obtenerPredioDelUsuarioActual(predioId);
        return evaluacionRepository
                .findByPredioIdOrderByFechaDesc(predio.getId(), PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamano, 1), 50)))
                .map(e -> {
                    List<Recomendacion> ranking = rankingDe(e);
                    Recomendacion mejor = ranking.isEmpty() ? null : ranking.get(0);
                    return new EvaluacionResumenDTO(e.getId(), e.getFecha(), e.getTipo(), e.getEstado(),
                            mejor != null ? mejor.getCultivo().getNombre() : null,
                            mejor != null ? mejor.getPuntaje() : null,
                            ranking.size());
                });
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluacionResponseDTO obtener(Long evaluacionId) {
        Long usuarioId = usuarioService.obtenerUsuarioActual().getId();
        Evaluacion e = evaluacionRepository.findByIdAndPredioUsuarioId(evaluacionId, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evaluación no encontrada"));
        return convertir(e);
    }

    // ------------------------------------------------------------------ auxiliares

    private Recomendacion crearRecomendacion(MotorReglasService.ResultadoCultivo r, int posicion) {
        Recomendacion rec = new Recomendacion();
        rec.setCultivo(r.cultivo());
        rec.setPosicion(posicion);
        rec.setPuntaje(r.puntaje());
        rec.setCompatibilidad(r.compatibilidad());
        for (MotorReglasService.Factor f : r.factores()) {
            FactorRecomendacion fr = new FactorRecomendacion();
            fr.setFactor(f.tipo());
            fr.setValorPredio(recortar(f.valorPredio(), 60));
            fr.setValorRequerido(recortar(f.valorRequerido(), 60));
            fr.setEfecto(f.efecto());
            fr.setAporte(f.aporte());
            fr.setDetalle(recortar(f.detalle(), 200));
            rec.agregarFactor(fr);
        }
        return rec;
    }

    /** Recomendaciones del ranking (no incluye un cultivo consultado que haya quedado fuera). */
    private List<Recomendacion> rankingDe(Evaluacion e) {
        return e.getRecomendaciones().stream()
                .filter(r -> r.getCompatibilidad() != Compatibilidad.BAJA && r.getPosicion() <= MAX_RANKING)
                .sorted(Comparator.comparing(Recomendacion::getPosicion))
                .toList();
    }

    private EvaluacionResponseDTO convertir(Evaluacion e) {
        EvaluacionResponseDTO dto = new EvaluacionResponseDTO();
        dto.setId(e.getId());
        dto.setPredioId(e.getPredio().getId());
        dto.setPredio(e.getPredio().getNombre());
        dto.setFecha(e.getFecha());
        dto.setTipo(e.getTipo());
        dto.setEstado(e.getEstado());
        if (e.getGrupoCum() != null) {
            dto.setGrupoCum(e.getGrupoCum().getCodigo());
            dto.setGrupoCumNombre(e.getGrupoCum().getNombre());
        }
        dto.setCumCodigoOriginal(e.getCumCodigoOriginal());
        dto.setCalidadAgrologica(e.getCalidadAgrologica());
        dto.setLimitacionesSuelo(e.getLimitacionesSuelo());
        dto.setAltitudMsnm(e.getAltitudMsnm());
        dto.setTemperaturaMedia(e.getTemperaturaMedia());
        dto.setPrecipitacionAnualMm(e.getPrecipitacionAnualMm());
        dto.setFuenteAgua(e.getFuenteAgua());
        dto.setNivelRiesgoHidrico(e.getNivelRiesgoHidrico());
        dto.setDistanciaPuntoCriticoKm(e.getDistanciaPuntoCriticoKm());
        dto.setFuentesFaltantes(e.getFuentesFaltantes() == null ? List.of()
                : Arrays.asList(e.getFuentesFaltantes().split(",")));
        dto.setExplicacion(e.getExplicacion());
        dto.setExplicacionPorIa(Boolean.TRUE.equals(e.getExplicacionPorIa()));
        dto.setModeloIa(e.getModeloIa());
        dto.setAviso(AVISO);
        dto.setRanking(rankingDe(e).stream().map(this::convertir).toList());
        if (e.getCultivoConsultado() != null) {
            Long id = e.getCultivoConsultado().getId();
            e.getRecomendaciones().stream()
                    .filter(r -> r.getCultivo().getId().equals(id))
                    .findFirst()
                    .ifPresent(r -> dto.setResultadoCultivoConsultado(convertir(r)));
        }
        return dto;
    }

    private RecomendacionDTO convertir(Recomendacion r) {
        List<FactorDTO> factores = r.getFactores().stream()
                .sorted(Comparator.comparing(FactorRecomendacion::getFactor))
                .map(f -> new FactorDTO(f.getFactor(), f.getValorPredio(), f.getValorRequerido(), f.getEfecto(),
                        f.getAporte(), f.getFactor().getPesoMaximo(), f.getDetalle()))
                .toList();
        return new RecomendacionDTO(r.getPosicion(), r.getCultivo().getId(), r.getCultivo().getNombre(),
                r.getCultivo().getTipo(), r.getPuntaje(), r.getCompatibilidad(), factores);
    }

    /** Explicación con plantilla: se usa cuando la IA no está configurada o no responde. */
    String generarExplicacion(Predio predio, GrupoCum grupo, MotorReglasService.DatosPredio d,
                              List<BuscadorPuntosCriticos.PuntoCercano> cercanos,
                              List<MotorReglasService.ResultadoCultivo> ranking,
                              MotorReglasService.ResultadoCultivo consultado, List<String> faltantes) {
        StringBuilder sb = new StringBuilder();
        if (grupo != null) {
            sb.append("Su predio ").append(predio.getNombre()).append(" está en ")
                    .append(grupo.getNombre().toLowerCase()).append(" (grupo ").append(grupo.getCodigo()).append(").");
        } else {
            sb.append("No encontramos una clasificación oficial de suelos para la ubicación de su predio ")
                    .append(predio.getNombre()).append(", por eso la evaluación se basó en altitud, clima y riesgo.");
        }
        if (d.altitudMsnm() != null) {
            sb.append(" Se encuentra a ").append(d.altitudMsnm()).append(" msnm");
            if (d.temperaturaMedia() != null && d.precipitacionAnualMm() != null) {
                sb.append(String.format(Locale.US, ", con una temperatura media de %.1f °C y unos %.0f mm de lluvia al año.",
                        d.temperaturaMedia(), d.precipitacionAnualMm()));
            } else {
                sb.append(".");
            }
        }
        if (cercanos.isEmpty()) {
            sb.append(" No se registran puntos críticos de riesgo hídrico cercanos.");
        } else {
            BuscadorPuntosCriticos.PuntoCercano pc = cercanos.get(0);
            sb.append(" Hay un punto crítico de riesgo hídrico ")
                    .append(pc.punto().getNivelRiesgo().name().toLowerCase().replace('_', ' '))
                    .append(" a ").append(pc.distanciaKm()).append(" km");
            sb.append(d.nivelRiesgo() != null && d.nivelRiesgo().esAltoOMayor()
                    ? "; conviene tomar medidas preventivas." : ".");
        }
        if (ranking.isEmpty()) {
            sb.append("X".equals(d.grupoCum())
                    ? " Por tratarse de tierras de protección, no se recomienda ningún cultivo."
                    : " Ningún cultivo del catálogo alcanzó una compatibilidad suficiente con las condiciones del predio.");
        } else {
            String mejores = ranking.stream().limit(3).map(r -> r.cultivo().getNombre())
                    .collect(Collectors.joining(", "));
            sb.append(" Los cultivos más compatibles son: ").append(mejores).append(".");
        }
        if (consultado != null) {
            sb.append(String.format(Locale.US, " El cultivo que consultó (%s) tiene compatibilidad %s (%.0f de 100).",
                    consultado.cultivo().getNombre(), consultado.compatibilidad().name().toLowerCase(),
                    consultado.puntaje()));
        }
        if (!faltantes.isEmpty()) {
            sb.append(" Nota: no se pudo obtener información de ").append(String.join(" y ", faltantes))
                    .append(", por lo que el resultado es parcial.");
        }
        return sb.toString();
    }

    private String recortar(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max);
    }
}
