package pe.edu.upc.agrocrew.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upc.agrocrew.exceptions.IntegracionException;
import pe.edu.upc.agrocrew.integraciones.GeminiClient;
import pe.edu.upc.agrocrew.models.FuenteAgua;
import pe.edu.upc.agrocrew.models.NivelRiesgo;
import pe.edu.upc.agrocrew.models.Predio;

import org.springframework.context.i18n.LocaleContextHolder;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExplicacionIaServiceTest {

    @Mock private GeminiClient geminiClient;

    @InjectMocks
    private ExplicacionIaService service;

    private final MotorReglasService.DatosPredio datos = new MotorReglasService.DatosPredio(
            "C", 1050, 24.0, 1500.0, FuenteAgua.RIEGO_GRAVEDAD, NivelRiesgo.BAJO);

    private Predio predio() {
        Predio p = new Predio();
        p.setNombre("Finca Quillabamba");
        return p;
    }

    @Test
    void conIdiomaIngles_elPromptPideRespuestaEnIngles() {
        LocaleContextHolder.setLocale(Locale.US);
        String prompt = service.construirPrompt(predio(), null, datos, List.of(), null);
        LocaleContextHolder.resetLocaleContext();

        assertTrue(prompt.contains("simple English"));
    }

    @Test
    void sinApiKey_noLlamaALaIaYDevuelveVacio() {
        when(geminiClient.habilitado()).thenReturn(false);

        assertTrue(service.generar(predio(), null, datos, List.of(), null).isEmpty());
        verify(geminiClient, never()).generarTexto(anyString());
    }

    @Test
    void conApiKey_devuelveElTextoYElModelo() {
        when(geminiClient.habilitado()).thenReturn(true);
        when(geminiClient.generarTexto(anyString())).thenReturn("Su predio es apto para cultivos permanentes.");
        when(geminiClient.getModelo()).thenReturn("gemini-2.5-flash");

        var r = service.generar(predio(), null, datos, List.of(), null).orElseThrow();

        assertEquals("gemini-2.5-flash", r.modelo());
        assertTrue(r.texto().contains("cultivos permanentes"));
    }

    @Test
    void siLaIaFalla_devuelveVacioParaUsarLaPlantilla() {
        when(geminiClient.habilitado()).thenReturn(true);
        when(geminiClient.generarTexto(anyString())).thenThrow(new IntegracionException("timeout"));

        assertTrue(service.generar(predio(), null, datos, List.of(), null).isEmpty());
    }

    @Test
    void elPromptSoloUsaDatosDeLaEvaluacion() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es-PE"));
        String prompt = service.construirPrompt(predio(), null, datos, List.of(), null);
        LocaleContextHolder.resetLocaleContext();

        assertTrue(prompt.contains("Finca Quillabamba"));
        assertTrue(prompt.contains("1050 msnm"));
        assertTrue(prompt.contains("no reemplaza a un ingeniero agrónomo"));
    }
}
