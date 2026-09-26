package pe.edu.upc.agrocrew.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agrocrew.dto.AlertaDTO;
import pe.edu.upc.agrocrew.dto.DiaPronosticoDTO;
import pe.edu.upc.agrocrew.exceptions.IntegracionException;
import pe.edu.upc.agrocrew.exceptions.RecursoNoEncontradoException;
import pe.edu.upc.agrocrew.integraciones.OpenMeteoClient;
import pe.edu.upc.agrocrew.models.*;
import pe.edu.upc.agrocrew.repositories.AlertaRepository;
import pe.edu.upc.agrocrew.repositories.PredioRepository;
import pe.edu.upc.agrocrew.services.AlertaService;
import pe.edu.upc.agrocrew.services.BuscadorPuntosCriticos;
import pe.edu.upc.agrocrew.services.UsuarioService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertaServiceImpl implements AlertaService {

    private static final String ACCIONES_RIESGO =
            "Revise y limpie canales y drenes; evite sembrar en las zonas más bajas del terreno; "
                    + "tenga a mano el contacto de Defensa Civil de su distrito; revise el pronóstico antes de sembrar.";
    private static final String ACCIONES_LLUVIA =
            "Asegure los drenajes; proteja semillas, fertilizantes y cosecha almacenada; "
                    + "postergue la aplicación de abonos o agroquímicos hasta que pase la lluvia.";

    private final AlertaRepository alertaRepository;
    private final PredioRepository predioRepository;
    private final BuscadorPuntosCriticos buscador;
    private final OpenMeteoClient openMeteoClient;
    private final UsuarioService usuarioService;

    @Value("${app.riesgo.radio-km:5}")
    private double radioKm;

    @Value("${app.alertas.umbral-lluvia-mm:30}")
    private double umbralLluviaMm;

    @Override
    public int generarAlertasRiesgo(Predio predio) {
        int creadas = 0;
        for (BuscadorPuntosCriticos.PuntoCercano pc :
                buscador.buscar(predio.getLatitud(), predio.getLongitud(), radioKm)) {
            PuntoCritico punto = pc.punto();
            if (!punto.getNivelRiesgo().esAltoOMayor()
                    || alertaRepository.existsByPredioIdAndPuntoCriticoIdAndLeidaFalse(predio.getId(), punto.getId())) {
                continue;
            }
            String peligro = punto.getTipoPeligro() != null ? punto.getTipoPeligro().toLowerCase() : "inundación o desborde";
            Alerta a = new Alerta();
            a.setPredio(predio);
            a.setPuntoCritico(punto);
            a.setTipo(TipoAlerta.RIESGO_HIDRICO);
            a.setNivel(punto.getNivelRiesgo());
            a.setTitulo(recortar("Riesgo " + nivelTexto(punto.getNivelRiesgo()) + " de " + peligro + " cerca de " + predio.getNombre(), 120));
            a.setMensaje("Su predio " + predio.getNombre() + " está a " + pc.distanciaKm()
                    + " km de un punto crítico identificado por la ANA"
                    + (punto.getDescripcion() != null ? ": " + punto.getDescripcion() : ".") );
            a.setAccionesSugeridas(ACCIONES_RIESGO);
            alertaRepository.save(a);
            creadas++;
        }
        if (creadas > 0) {
            log.info("Alertas de riesgo creadas para el predio {}: {}", predio.getId(), creadas);
        }
        return creadas;
    }

    @Override
    @Transactional
    public int generarAlertasRiesgoParaTodos() {
        int total = 0;
        for (Predio p : predioRepository.findByActivoTrue()) {
            total += generarAlertasRiesgo(p);
        }
        return total;
    }

    @Override
    @Transactional
    public int revisarLluviasIntensas() {
        int creadas = 0;
        LocalDateTime inicioDelDia = LocalDate.now().atStartOfDay();
        for (Predio predio : predioRepository.findByActivoTrue()) {
            if (alertaRepository.existsByPredioIdAndTipoAndFechaGeneracionAfter(
                    predio.getId(), TipoAlerta.LLUVIA_INTENSA, inicioDelDia)) {
                continue;
            }
            try {
                DiaPronosticoDTO dia = openMeteoClient.obtenerPronostico(predio.getLatitud(), predio.getLongitud())
                        .stream()
                        .filter(d -> d.precipitacionMm() != null && d.precipitacionMm() >= umbralLluviaMm)
                        .findFirst().orElse(null);
                if (dia == null) {
                    continue;
                }
                NivelRiesgo nivel = dia.precipitacionMm() >= umbralLluviaMm * 2 ? NivelRiesgo.MUY_ALTO : NivelRiesgo.ALTO;
                Alerta a = new Alerta();
                a.setPredio(predio);
                a.setTipo(TipoAlerta.LLUVIA_INTENSA);
                a.setNivel(nivel);
                a.setTitulo(recortar("Lluvia intensa prevista en " + predio.getNombre(), 120));
                a.setMensaje(String.format(Locale.US, "Se esperan %.1f mm de lluvia el %s en la zona de su predio.",
                        dia.precipitacionMm(), dia.fecha()));
                a.setAccionesSugeridas(ACCIONES_LLUVIA);
                alertaRepository.save(a);
                creadas++;
            } catch (IntegracionException e) {
                log.warn("No se pudo revisar el pronóstico del predio {}: {}", predio.getId(), e.getMessage());
            }
        }
        log.info("Revisión de lluvias intensas terminada. Alertas creadas: {}", creadas);
        return creadas;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertaDTO> listarMisAlertas(Boolean leida) {
        Long usuarioId = usuarioService.obtenerUsuarioActual().getId();
        List<Alerta> alertas = leida == null
                ? alertaRepository.findByPredioUsuarioIdOrderByFechaGeneracionDesc(usuarioId)
                : alertaRepository.findByPredioUsuarioIdAndLeidaOrderByFechaGeneracionDesc(usuarioId, leida);
        return alertas.stream().map(this::convertir).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long contarNoLeidas() {
        return alertaRepository.countByPredioUsuarioIdAndLeidaFalse(usuarioService.obtenerUsuarioActual().getId());
    }

    @Override
    @Transactional
    public void marcarComoLeida(Long alertaId) {
        Long usuarioId = usuarioService.obtenerUsuarioActual().getId();
        Alerta a = alertaRepository.findByIdAndPredioUsuarioId(alertaId, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alerta no encontrada"));
        if (!Boolean.TRUE.equals(a.getLeida())) {
            a.setLeida(true);
            a.setFechaLectura(LocalDateTime.now());
            alertaRepository.save(a);
        }
    }

    private AlertaDTO convertir(Alerta a) {
        AlertaDTO dto = new AlertaDTO();
        dto.setId(a.getId());
        dto.setPredioId(a.getPredio().getId());
        dto.setPredio(a.getPredio().getNombre());
        dto.setTipo(a.getTipo());
        dto.setNivel(a.getNivel());
        dto.setTitulo(a.getTitulo());
        dto.setMensaje(a.getMensaje());
        dto.setAccionesSugeridas(a.getAccionesSugeridas());
        dto.setFechaGeneracion(a.getFechaGeneracion());
        dto.setLeida(a.getLeida());
        dto.setFechaLectura(a.getFechaLectura());
        return dto;
    }

    private String nivelTexto(NivelRiesgo n) {
        return n == NivelRiesgo.MUY_ALTO ? "muy alto" : n.name().toLowerCase();
    }

    private String recortar(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
}
