package pe.edu.upc.agrocrew.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.edu.upc.agrocrew.models.PuntoCritico;
import pe.edu.upc.agrocrew.repositories.PuntoCriticoRepository;
import pe.edu.upc.agrocrew.util.GeoUtils;

import java.util.Comparator;
import java.util.List;

/**
 * Busca los puntos críticos de la ANA cercanos a una coordenada.
 * Primero filtra por un recuadro en la base de datos y luego calcula la distancia exacta (Haversine).
 */
@Component
@RequiredArgsConstructor
public class BuscadorPuntosCriticos {

    private final PuntoCriticoRepository puntoCriticoRepository;

    public record PuntoCercano(PuntoCritico punto, double distanciaKm) {
    }

    public List<PuntoCercano> buscar(double latitud, double longitud, double radioKm) {
        double dLat = GeoUtils.gradosLatitud(radioKm);
        double dLng = GeoUtils.gradosLongitud(radioKm, latitud);
        return puntoCriticoRepository
                .findByLatitudBetweenAndLongitudBetween(latitud - dLat, latitud + dLat, longitud - dLng, longitud + dLng)
                .stream()
                .map(p -> new PuntoCercano(p, GeoUtils.redondear(
                        GeoUtils.distanciaKm(latitud, longitud, p.getLatitud(), p.getLongitud()), 2)))
                .filter(pc -> pc.distanciaKm() <= radioKm)
                .sorted(Comparator.comparingDouble(PuntoCercano::distanciaKm))
                .toList();
    }
}
