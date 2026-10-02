package pe.edu.upc.agrocrew.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeoUtilsTest {

    @Test
    void distanciaKm_mismoPunto_esCero() {
        assertEquals(0.0, GeoUtils.distanciaKm(-14.07, -75.73, -14.07, -75.73), 1e-9);
    }

    @Test
    void distanciaKm_esSimetrica() {
        double ida = GeoUtils.distanciaKm(-12.0464, -77.0428, -14.0678, -75.7286);
        double vuelta = GeoUtils.distanciaKm(-14.0678, -75.7286, -12.0464, -77.0428);
        assertEquals(ida, vuelta, 1e-9);
    }

    @Test
    void distanciaKm_unGradoDeLatitud_sonUnos111Km() {
        assertEquals(111.19, GeoUtils.distanciaKm(0, 0, 1, 0), 0.01);
    }

    @Test
    void distanciaKm_limaIca_ronda266Km() {
        double km = GeoUtils.distanciaKm(-12.0464, -77.0428, -14.0678, -75.7286);
        assertEquals(266.0, km, 1.0);
    }

    @Test
    void distanciaKm_puntosOpuestos_esMediaCircunferencia() {
        assertEquals(20015.09, GeoUtils.distanciaKm(0, 0, 0, 180), 0.1);
    }

    @Test
    void gradosLatitud_111Km_equivalenAUnGrado() {
        assertEquals(1.0, GeoUtils.gradosLatitud(111.0), 1e-9);
    }

    @Test
    void gradosLongitud_enElEcuador_coincideConLaLatitud() {
        assertEquals(GeoUtils.gradosLatitud(50.0), GeoUtils.gradosLongitud(50.0, 0), 1e-9);
    }

    @Test
    void gradosLongitud_aMayorLatitud_hacenFaltaMasGrados() {
        assertEquals(2.0, GeoUtils.gradosLongitud(111.0, 60), 1e-9);
        assertTrue(GeoUtils.gradosLongitud(10.0, 60) > GeoUtils.gradosLongitud(10.0, 0));
    }

    @Test
    void redondear_dosDecimales() {
        assertEquals(3.14, GeoUtils.redondear(3.14159, 2), 1e-9);
    }

    @Test
    void redondear_sinDecimales() {
        assertEquals(2.0, GeoUtils.redondear(2.4, 0), 1e-9);
        assertEquals(3.0, GeoUtils.redondear(2.5, 0), 1e-9);
    }
}
