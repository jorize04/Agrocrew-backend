package pe.edu.upc.agrocrew.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.edu.upc.agrocrew.models.*;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas del motor de reglas (T27). No necesita mocks, porque no depende de la base de datos. */
class MotorReglasServiceTest {

    private MotorReglasService motor;
    private Cultivo papa;
    private Cultivo mango;

    @BeforeEach
    void setUp() {
        motor = new MotorReglasService();
        papa = cultivo(1L, "Papa", Set.of("A"), 2500, 4200, 8, 20, 500, 1200, ToleranciaInundacion.BAJA, false);
        mango = cultivo(2L, "Mango", Set.of("C"), 0, 1000, 22, 35, 700, 1500, ToleranciaInundacion.BAJA, true);
    }

    @Test
    void predioAndinoIdeal_papaTieneCompatibilidadAltaConPuntajeMaximo() {
        var datos = new MotorReglasService.DatosPredio("A", 3300, 11.0, 750.0, FuenteAgua.SECANO, NivelRiesgo.BAJO);

        var r = motor.evaluarCultivo(datos, papa);

        assertEquals(100.0, r.puntaje());
        assertEquals(Compatibilidad.ALTA, r.compatibilidad());
        assertTrue(r.factores().stream().allMatch(f -> f.efecto() == EfectoFactor.FAVORABLE));
    }

    @Test
    void mangoEnLaSierra_quedaConCompatibilidadBajaPorAltitudYTemperatura() {
        var datos = new MotorReglasService.DatosPredio("A", 3300, 11.0, 750.0, FuenteAgua.SECANO, NivelRiesgo.BAJO);

        var r = motor.evaluarCultivo(datos, mango);

        assertEquals(Compatibilidad.BAJA, r.compatibilidad());
        assertTrue(r.puntaje() < 50);
    }

    @Test
    void tierraDeGrupoA_admiteCultivosPermanentesDeGrupoC() {
        var datos = new MotorReglasService.DatosPredio("A", 300, 26.0, 1000.0, FuenteAgua.RIEGO_GRAVEDAD, NivelRiesgo.BAJO);

        var suelo = motor.evaluarCultivo(datos, mango).factores().get(0);

        assertEquals(TipoFactor.SUELO, suelo.tipo());
        assertEquals(EfectoFactor.FAVORABLE, suelo.efecto());
    }

    @Test
    void tierraDeGrupoC_noAdmiteCultivosEnLimpio() {
        var datos = new MotorReglasService.DatosPredio("C", 3300, 11.0, 750.0, FuenteAgua.SECANO, NivelRiesgo.BAJO);

        var r = motor.evaluarCultivo(datos, papa);

        assertEquals(EfectoFactor.LIMITANTE, r.factores().get(0).efecto());
        assertEquals(Compatibilidad.BAJA, r.compatibilidad());
    }

    @Test
    void tierrasDeProteccion_ningunCultivoEsCompatible() {
        var datos = new MotorReglasService.DatosPredio("X", 3300, 11.0, 750.0, FuenteAgua.SECANO, NivelRiesgo.BAJO);

        var resultados = motor.evaluar(datos, List.of(papa, mango));

        assertTrue(resultados.stream().allMatch(r -> r.compatibilidad() == Compatibilidad.BAJA));
    }

    @Test
    void sinDatosDeSueloNiClima_losFactoresQuedanNeutrosYNoFalla() {
        var datos = new MotorReglasService.DatosPredio(null, 3300, null, null, FuenteAgua.SECANO, null);

        var r = motor.evaluarCultivo(datos, papa);

        assertEquals(EfectoFactor.NEUTRO, r.factores().get(0).efecto());   // suelo
        assertEquals(EfectoFactor.NEUTRO, r.factores().get(2).efecto());   // temperatura
        assertEquals(Compatibilidad.MEDIA, r.compatibilidad());
    }

    @Test
    void riegoCompensaLaFaltaDeLluvia() {
        var secano = new MotorReglasService.DatosPredio("A", 3300, 11.0, 200.0, FuenteAgua.SECANO, NivelRiesgo.BAJO);
        var riego = new MotorReglasService.DatosPredio("A", 3300, 11.0, 200.0, FuenteAgua.RIEGO_TECNIFICADO, NivelRiesgo.BAJO);

        assertEquals(EfectoFactor.LIMITANTE, motor.evaluarCultivo(secano, papa).factores().get(3).efecto());
        assertEquals(EfectoFactor.FAVORABLE, motor.evaluarCultivo(riego, papa).factores().get(3).efecto());
    }

    @Test
    void riesgoAltoConCultivoPocoTolerante_esLimitanteYNoPuedeSerAlta() {
        var datos = new MotorReglasService.DatosPredio("A", 3300, 11.0, 750.0, FuenteAgua.SECANO, NivelRiesgo.ALTO);

        var r = motor.evaluarCultivo(datos, papa);

        assertEquals(EfectoFactor.LIMITANTE, r.factores().get(4).efecto());
        assertEquals(Compatibilidad.MEDIA, r.compatibilidad());
    }

    @Test
    void mismosDatos_mismoResultado() {
        var datos = new MotorReglasService.DatosPredio("A", 3000, 12.0, 700.0, FuenteAgua.SECANO, NivelRiesgo.MEDIO);

        assertEquals(motor.evaluar(datos, List.of(papa, mango)), motor.evaluar(datos, List.of(papa, mango)));
    }

    private Cultivo cultivo(Long id, String nombre, Set<String> grupos, int altMin, int altMax,
                            double tMin, double tMax, double pMin, double pMax,
                            ToleranciaInundacion tol, boolean riego) {
        Cultivo c = new Cultivo();
        c.setId(id);
        c.setNombre(nombre);
        c.setTipo(TipoCultivo.TRANSITORIO);
        for (String g : grupos) {
            GrupoCum gc = new GrupoCum();
            gc.setCodigo(g);
            c.getGruposCum().add(gc);
        }
        RequerimientoCultivo r = new RequerimientoCultivo();
        r.setAltitudMin(altMin);
        r.setAltitudMax(altMax);
        r.setTemperaturaMin(tMin);
        r.setTemperaturaMax(tMax);
        r.setPrecipitacionMinMm(pMin);
        r.setPrecipitacionMaxMm(pMax);
        r.setToleranciaInundacion(tol);
        r.setRequiereRiego(riego);
        c.asignarRequerimiento(r);
        return c;
    }
}
