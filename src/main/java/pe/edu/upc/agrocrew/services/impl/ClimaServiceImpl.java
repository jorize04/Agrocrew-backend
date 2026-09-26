package pe.edu.upc.agrocrew.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agrocrew.dto.ClimaDTO;
import pe.edu.upc.agrocrew.dto.DiaPronosticoDTO;
import pe.edu.upc.agrocrew.exceptions.IntegracionException;
import pe.edu.upc.agrocrew.integraciones.OpenMeteoClient;
import pe.edu.upc.agrocrew.models.DatoClimatico;
import pe.edu.upc.agrocrew.models.Predio;
import pe.edu.upc.agrocrew.repositories.DatoClimaticoRepository;
import pe.edu.upc.agrocrew.services.ClimaService;
import pe.edu.upc.agrocrew.services.PredioService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClimaServiceImpl implements ClimaService {

    private static final String FUENTE = "Open-Meteo (ERA5 y pronóstico)";

    private final PredioService predioService;
    private final OpenMeteoClient openMeteoClient;
    private final DatoClimaticoRepository datoClimaticoRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ClimaDTO obtenerClimaDePredio(Long predioId) {
        Predio predio = predioService.obtenerPredioDelUsuarioActual(predioId);
        try {
            OpenMeteoClient.ClimaAnual anual = openMeteoClient.obtenerClimaAnual(predio.getLatitud(), predio.getLongitud());
            List<DiaPronosticoDTO> pronostico = openMeteoClient.obtenerPronostico(predio.getLatitud(), predio.getLongitud());
            DatoClimatico dato = guardar(predio, anual, pronostico);
            return new ClimaDTO(predio.getId(), anual.temperaturaMedia(), anual.precipitacionAnualMm(),
                    pronostico, dato.getFechaConsulta(), false, FUENTE);
        } catch (IntegracionException e) {
            DatoClimatico guardado = datoClimaticoRepository.findFirstByPredioIdOrderByFechaConsultaDesc(predio.getId())
                    .orElseThrow(() -> new IntegracionException(
                            "El clima no está disponible temporalmente. Intente más tarde."));
            log.warn("Open-Meteo no respondió; se devuelven datos guardados del predio {}", predio.getId());
            return new ClimaDTO(predio.getId(), guardado.getTemperaturaMedia(), guardado.getPrecipitacionAnualMm(),
                    leerPronostico(guardado.getPronostico()), guardado.getFechaConsulta(), true, FUENTE);
        }
    }

    /** noRollbackFor: si Open-Meteo falla, la evaluación que llamó a este método debe poder continuar. */
    @Override
    @Transactional(noRollbackFor = IntegracionException.class)
    public OpenMeteoClient.ClimaAnual obtenerClimaAnual(Predio predio) {
        try {
            OpenMeteoClient.ClimaAnual anual = openMeteoClient.obtenerClimaAnual(predio.getLatitud(), predio.getLongitud());
            guardar(predio, anual, null);
            return anual;
        } catch (IntegracionException e) {
            Optional<DatoClimatico> guardado =
                    datoClimaticoRepository.findFirstByPredioIdOrderByFechaConsultaDesc(predio.getId());
            if (guardado.isPresent() && guardado.get().getTemperaturaMedia() != null) {
                return new OpenMeteoClient.ClimaAnual(guardado.get().getTemperaturaMedia(),
                        guardado.get().getPrecipitacionAnualMm());
            }
            throw e;
        }
    }

    private DatoClimatico guardar(Predio predio, OpenMeteoClient.ClimaAnual anual, List<DiaPronosticoDTO> pronostico) {
        DatoClimatico dato = new DatoClimatico();
        dato.setPredio(predio);
        dato.setFechaConsulta(LocalDateTime.now());
        dato.setTemperaturaMedia(anual.temperaturaMedia());
        dato.setPrecipitacionAnualMm(anual.precipitacionAnualMm());
        if (pronostico != null) {
            try {
                dato.setPronostico(objectMapper.writeValueAsString(pronostico));
            } catch (Exception e) {
                log.warn("No se pudo guardar el pronóstico: {}", e.getMessage());
            }
        }
        return datoClimaticoRepository.save(dato);
    }

    private List<DiaPronosticoDTO> leerPronostico(String json) {
        if (json == null) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() { });
        } catch (Exception e) {
            return List.of();
        }
    }
}
