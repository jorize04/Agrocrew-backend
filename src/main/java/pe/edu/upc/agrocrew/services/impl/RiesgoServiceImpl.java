package pe.edu.upc.agrocrew.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agrocrew.dto.PuntoCriticoDTO;
import pe.edu.upc.agrocrew.dto.RiesgoDTO;
import pe.edu.upc.agrocrew.exceptions.ReglaNegocioException;
import pe.edu.upc.agrocrew.models.NivelRiesgo;
import pe.edu.upc.agrocrew.models.Predio;
import pe.edu.upc.agrocrew.services.BuscadorPuntosCriticos;
import pe.edu.upc.agrocrew.services.PredioService;
import pe.edu.upc.agrocrew.services.RiesgoService;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RiesgoServiceImpl implements RiesgoService {

    private final PredioService predioService;
    private final BuscadorPuntosCriticos buscador;

    @Value("${app.riesgo.radio-km:5}")
    private double radioKm;

    @Override
    @Transactional(readOnly = true)
    public RiesgoDTO consultarRiesgoDePredio(Long predioId) {
        Predio predio = predioService.obtenerPredioDelUsuarioActual(predioId);
        List<BuscadorPuntosCriticos.PuntoCercano> cercanos =
                buscador.buscar(predio.getLatitud(), predio.getLongitud(), radioKm);

        if (cercanos.isEmpty()) {
            return new RiesgoDTO(predio.getId(), NivelRiesgo.BAJO, radioKm, null, List.of(),
                    "No se registran puntos críticos de la ANA a menos de " + (int) radioKm
                            + " km. Esto no descarta todo riesgo: consulte también el pronóstico de lluvias.");
        }
        NivelRiesgo nivel = cercanos.stream().map(pc -> pc.punto().getNivelRiesgo())
                .max(Comparator.naturalOrder()).orElse(NivelRiesgo.BAJO);
        List<PuntoCriticoDTO> puntos = cercanos.stream().map(this::convertir).toList();
        PuntoCriticoDTO masCercano = puntos.get(0);
        String mensaje = "Hay " + puntos.size() + " punto(s) crítico(s) a menos de " + (int) radioKm
                + " km. El más cercano está a " + masCercano.getDistanciaKm() + " km"
                + (masCercano.getTipoPeligro() != null ? " (" + masCercano.getTipoPeligro().toLowerCase() + ")" : "")
                + ". Nivel de riesgo: " + nivel + ".";
        return new RiesgoDTO(predio.getId(), nivel, radioKm, masCercano, puntos, mensaje);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuntoCriticoDTO> listarCercanos(double latitud, double longitud, double radio) {
        if (radio <= 0 || radio > 50) {
            throw new ReglaNegocioException("El radio debe estar entre 1 y 50 km");
        }
        return buscador.buscar(latitud, longitud, radio).stream().map(this::convertir).toList();
    }

    private PuntoCriticoDTO convertir(BuscadorPuntosCriticos.PuntoCercano pc) {
        var p = pc.punto();
        return new PuntoCriticoDTO(p.getId(), p.getCodigoAna(), p.getDescripcion(), p.getTipoPeligro(),
                p.getNivelRiesgo(), p.getLatitud(), p.getLongitud(), p.getUbicacion(), pc.distanciaKm());
    }
}
