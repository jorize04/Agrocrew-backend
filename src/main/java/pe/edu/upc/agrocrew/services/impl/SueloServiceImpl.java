package pe.edu.upc.agrocrew.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agrocrew.dto.GrupoCumDTO;
import pe.edu.upc.agrocrew.dto.SueloDTO;
import pe.edu.upc.agrocrew.integraciones.MidagriClient;
import pe.edu.upc.agrocrew.models.GrupoCum;
import pe.edu.upc.agrocrew.models.Predio;
import pe.edu.upc.agrocrew.repositories.GrupoCumRepository;
import pe.edu.upc.agrocrew.services.PredioService;
import pe.edu.upc.agrocrew.services.SueloService;
import pe.edu.upc.agrocrew.util.CumParser;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SueloServiceImpl implements SueloService {

    private static final String FUENTE = "MIDAGRI - Clasificación de Tierras por su Capacidad de Uso Mayor";

    private final PredioService predioService;
    private final MidagriClient midagriClient;
    private final GrupoCumRepository grupoCumRepository;

    @Override
    @Transactional(readOnly = true)
    public SueloDTO consultarSueloDePredio(Long predioId) {
        Predio predio = predioService.obtenerPredioDelUsuarioActual(predioId);
        // Si MIDAGRI no responde se lanza IntegracionException -> HTTP 503.
        Optional<CumParser.Cum> cum = midagriClient.consultarCum(predio.getLatitud(), predio.getLongitud());

        if (cum.isEmpty()) {
            return new SueloDTO(predio.getId(), false, null, null, null, null,
                    "No hay una clasificación oficial de suelos para esta ubicación. "
                            + "La recomendación se basará en altitud, clima y riesgo hídrico.", FUENTE);
        }
        CumParser.Cum c = cum.get();
        GrupoCumDTO grupo = grupoCumRepository.findByCodigo(c.grupo())
                .map(this::convertir)
                .orElse(null);
        return new SueloDTO(predio.getId(), true, c.codigoOriginal(), grupo, c.calidad(), c.limitaciones(),
                grupo != null ? grupo.getDescripcionSimple() : null, FUENTE);
    }

    private GrupoCumDTO convertir(GrupoCum g) {
        return new GrupoCumDTO(g.getCodigo(), g.getNombre(), g.getDescripcionSimple(),
                g.getUsosRecomendados(), g.getUsosNoRecomendados());
    }
}
