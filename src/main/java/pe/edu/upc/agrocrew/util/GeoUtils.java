package pe.edu.upc.agrocrew.util;

/** Cálculos geográficos simples. */
public final class GeoUtils {

    private static final double RADIO_TIERRA_KM = 6371.0;

    private GeoUtils() {
    }

    /** Distancia en km entre dos coordenadas (fórmula de Haversine). */
    public static double distanciaKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return RADIO_TIERRA_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /** Grados de latitud equivalentes a una distancia (para armar un recuadro de búsqueda). */
    public static double gradosLatitud(double km) {
        return km / 111.0;
    }

    /** Grados de longitud equivalentes a una distancia en una latitud dada. */
    public static double gradosLongitud(double km, double latitud) {
        return km / (111.0 * Math.cos(Math.toRadians(latitud)));
    }

    public static double redondear(double valor, int decimales) {
        double factor = Math.pow(10, decimales);
        return Math.round(valor * factor) / factor;
    }
}
