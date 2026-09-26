package pe.edu.upc.agrocrew.integraciones;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.edu.upc.agrocrew.dto.DiaPronosticoDTO;
import pe.edu.upc.agrocrew.exceptions.IntegracionException;
import pe.edu.upc.agrocrew.models.ServicioExterno;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cliente de Open-Meteo (gratuito, sin API key).
 * - Clima anual: API histórica (ERA5), últimos 12 meses.
 * - Pronóstico: 7 días.
 */
@Component
@RequiredArgsConstructor
public class OpenMeteoClient {

    private final ClienteExterno clienteExterno;

    @Value("${app.integraciones.open-meteo.archivo-url}")
    private String urlArchivo;

    @Value("${app.integraciones.open-meteo.pronostico-url}")
    private String urlPronostico;

    /** Temperatura media y lluvia acumulada de los últimos 12 meses. */
    public record ClimaAnual(Double temperaturaMedia, Double precipitacionAnualMm) {
    }

    public ClimaAnual obtenerClimaAnual(double latitud, double longitud) {
        // El archivo histórico tiene unos días de retraso, por eso terminamos hace una semana.
        LocalDate fin = LocalDate.now().minusDays(7);
        LocalDate inicio = fin.minusDays(364);

        Map<String, String> p = new LinkedHashMap<>();
        p.put("latitude", String.valueOf(latitud));
        p.put("longitude", String.valueOf(longitud));
        p.put("start_date", inicio.toString());
        p.put("end_date", fin.toString());
        p.put("daily", "temperature_2m_mean,precipitation_sum");
        p.put("timezone", "America/Lima");

        JsonNode diario = clienteExterno.getJson(ServicioExterno.OPEN_METEO,
                ClienteExterno.construirUri(urlArchivo, p)).path("daily");

        double sumaTemp = 0;
        int diasTemp = 0;
        double sumaLluvia = 0;
        int diasLluvia = 0;
        for (JsonNode t : diario.path("temperature_2m_mean")) {
            if (!t.isNull()) {
                sumaTemp += t.asDouble();
                diasTemp++;
            }
        }
        for (JsonNode pr : diario.path("precipitation_sum")) {
            if (!pr.isNull()) {
                sumaLluvia += pr.asDouble();
                diasLluvia++;
            }
        }
        if (diasTemp == 0 || diasLluvia == 0) {
            throw new IntegracionException("Open-Meteo no devolvió datos de clima para esta ubicación");
        }
        // Si faltan algunos días de lluvia, se proyecta a 365 días.
        double lluviaAnual = sumaLluvia * 365.0 / diasLluvia;
        return new ClimaAnual(redondear(sumaTemp / diasTemp), redondear(lluviaAnual));
    }

    public List<DiaPronosticoDTO> obtenerPronostico(double latitud, double longitud) {
        Map<String, String> p = new LinkedHashMap<>();
        p.put("latitude", String.valueOf(latitud));
        p.put("longitude", String.valueOf(longitud));
        p.put("daily", "temperature_2m_max,temperature_2m_min,precipitation_sum");
        p.put("forecast_days", "7");
        p.put("timezone", "America/Lima");

        JsonNode diario = clienteExterno.getJson(ServicioExterno.OPEN_METEO,
                ClienteExterno.construirUri(urlPronostico, p)).path("daily");

        List<DiaPronosticoDTO> dias = new ArrayList<>();
        JsonNode fechas = diario.path("time");
        for (int i = 0; i < fechas.size(); i++) {
            dias.add(new DiaPronosticoDTO(
                    LocalDate.parse(fechas.get(i).asText()),
                    valor(diario.path("temperature_2m_max").get(i)),
                    valor(diario.path("temperature_2m_min").get(i)),
                    valor(diario.path("precipitation_sum").get(i))));
        }
        return dias;
    }

    private Double valor(JsonNode nodo) {
        return nodo == null || nodo.isNull() ? null : nodo.asDouble();
    }

    private double redondear(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
