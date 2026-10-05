package pe.edu.upc.agrocrew.util;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class CumParserTest {

    @Test
    void simboloConCalidadYLimitaciones() {
        var cum = CumParser.desdeSimbolo("A2se").orElseThrow();
        assertEquals("A", cum.grupo());
        assertEquals("MEDIA", cum.calidad());
        assertEquals("suelo, erosión", cum.limitaciones());
    }

    @Test
    void asociacion_tomaLaPrimeraClasificacion() {
        assertEquals("C", CumParser.desdeSimbolo("C3se-P2sec").orElseThrow().grupo());
    }

    @Test
    void textoDescriptivo() {
        assertEquals("P", CumParser.desdeTexto("Tierras Aptas para Pastos Temporales").orElseThrow().grupo());
        assertEquals("X", CumParser.desdeTexto("Tierras de Protección").orElseThrow().grupo());
    }

    @Test
    void atributos_priorizaCamposConNombreDeCum_yNoConfundeUnaLetraSuelta() {
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("ESTADO", "A");          // no debe interpretarse como grupo A
        a.put("GRUPOCUM", "F3e");
        assertEquals("F", CumParser.desdeAtributos(a).orElseThrow().grupo());
    }

    @Test
    void sinDatosReconocibles_devuelveVacio() {
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("OBJECTID", 15);
        a.put("NOMBRE", "Sector Alto");
        assertTrue(CumParser.desdeAtributos(a).isEmpty());
    }

    @Test
    void formatoSerfor_priorizaElSimboloParaObtenerCalidadYLimitaciones() {
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("GRUPOCUM", "Tierras aptas para cultivo en limpio");
        a.put("SIMCUM", "A2sc");
        a.put("DESCUM", "Calidad agrológica media, limitada por suelo y clima");

        var cum = CumParser.desdeAtributos(a).orElseThrow();

        assertEquals("A", cum.grupo());
        assertEquals("MEDIA", cum.calidad());
        assertEquals("suelo, clima", cum.limitaciones());
    }

    @Test
    void simboloConAnotacionEntreParentesis_casoRealDelSerfor() {
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("GRUPOCUM", "C");
        a.put("SIMCUM", "C3s(r)");

        var cum = CumParser.desdeAtributos(a).orElseThrow();

        assertEquals("C", cum.grupo());
        assertEquals("BAJA", cum.calidad());
        assertEquals("suelo", cum.limitaciones());
        assertEquals("C3s(r)", cum.codigoOriginal());
    }
}
