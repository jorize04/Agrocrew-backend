package pe.edu.upc.agrocrew.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agrocrew.dto.PuntoCriticoDTO;
import pe.edu.upc.agrocrew.dto.PuntoCriticoRequestDTO;
import pe.edu.upc.agrocrew.dto.SincronizacionDTO;
import pe.edu.upc.agrocrew.exceptions.ConflictoException;
import pe.edu.upc.agrocrew.integraciones.AnaClient;
import pe.edu.upc.agrocrew.models.NivelRiesgo;
import pe.edu.upc.agrocrew.models.PuntoCritico;
import pe.edu.upc.agrocrew.repositories.PuntoCriticoRepository;
import pe.edu.upc.agrocrew.services.AlertaService;
import pe.edu.upc.agrocrew.services.PuntoCriticoService;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PuntoCriticoServiceImpl implements PuntoCriticoService {

    private final AnaClient anaClient;
    private final PuntoCriticoRepository puntoCriticoRepository;
    private final AlertaService alertaService;

    @Override
    @Transactional
    public SincronizacionDTO sincronizarConAna() {
        // Si la ANA no responde se lanza IntegracionException -> HTTP 503 y no se modifica nada.
        List<AnaClient.PuntoCriticoExterno> externos = anaClient.descargarPuntosCriticos();
        LocalDateTime ahora = LocalDateTime.now();
        int nuevos = 0;
        int actualizados = 0;
        Set<String> procesados = new HashSet<>();

        for (AnaClient.PuntoCriticoExterno e : externos) {
            String codigo = "ANA-" + e.codigo();
            if (!procesados.add(codigo)) {
                continue;
            }
            PuntoCritico p = puntoCriticoRepository.findByCodigoAna(codigo).orElse(null);
            if (p == null) {
                p = new PuntoCritico();
                p.setCodigoAna(codigo);
                nuevos++;
            } else {
                actualizados++;
            }
            p.setDescripcion(e.descripcion());
            p.setTipoPeligro(e.tipoPeligro());
            p.setNivelRiesgo(e.nivel());
            p.setLatitud(e.latitud());
            p.setLongitud(e.longitud());
            p.setUbicacion(e.ubicacion());
            p.setFechaSincronizacion(ahora);
            puntoCriticoRepository.save(p);
        }
        int alertas = alertaService.generarAlertasRiesgoParaTodos();
        return new SincronizacionDTO(externos.size(), nuevos, actualizados, alertas);
    }

    @Override
    @Transactional
    public PuntoCriticoDTO registrarManual(PuntoCriticoRequestDTO dto) {
        String codigo = dto.getCodigo().trim();
        if (puntoCriticoRepository.findByCodigoAna(codigo).isPresent()) {
            throw new ConflictoException("Ya existe un punto crítico con el código " + codigo);
        }
        PuntoCritico p = new PuntoCritico();
        p.setCodigoAna(codigo);
        p.setDescripcion(dto.getDescripcion());
        p.setTipoPeligro(dto.getTipoPeligro());
        p.setNivelRiesgo(dto.getNivelRiesgo());
        p.setLatitud(dto.getLatitud());
        p.setLongitud(dto.getLongitud());
        p.setFechaSincronizacion(LocalDateTime.now());
        PuntoCritico guardado = puntoCriticoRepository.save(p);
        int alertas = alertaService.generarAlertasRiesgoParaTodos();
        log.info("Punto crítico manual registrado {} (alertas generadas: {})", codigo, alertas);
        return new PuntoCriticoDTO(guardado.getId(), guardado.getCodigoAna(), guardado.getDescripcion(),
                guardado.getTipoPeligro(), guardado.getNivelRiesgo(), guardado.getLatitud(), guardado.getLongitud(),
                guardado.getUbicacion(), null);
    }
}
