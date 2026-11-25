package pe.edu.upc.agrocrew.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upc.agrocrew.dto.CatastroDTO;
import pe.edu.upc.agrocrew.exceptions.IntegracionException;
import pe.edu.upc.agrocrew.exceptions.RecursoNoEncontradoException;
import pe.edu.upc.agrocrew.exceptions.ReglaNegocioException;
import pe.edu.upc.agrocrew.integraciones.CatastroClient;
import pe.edu.upc.agrocrew.models.Departamento;
import pe.edu.upc.agrocrew.models.Distrito;
import pe.edu.upc.agrocrew.repositories.DepartamentoRepository;
import pe.edu.upc.agrocrew.repositories.DistritoRepository;
import pe.edu.upc.agrocrew.services.impl.CatastroServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatastroServiceImplTest {

    @Mock private CatastroClient catastroClient;
    @Mock private DepartamentoRepository departamentoRepository;
    @Mock private DistritoRepository distritoRepository;

    @InjectMocks
    private CatastroServiceImpl catastroService;

    @Test
    void leerPredio_respuestaArcGis_obtieneDatosAreaYCentroDelPoligono() throws Exception {
        // Cuadrado de 0.002° alrededor de (-11.2, -74.6), con el campo de área propio de la capa de Junín.
        String json = """
                {"attributes": {"UNICAT": "706980", "NOMPRE": "VILLA VICTORIA I", "DEPAR": "JUNIN",
                  "PROVI": "SATIPO", "DISTR": "COVIRIALI", "UBIDIS": "120602",
                  "SERFOR_IDE.USUIDE.PRED_JUNIN.AREA": 1.9793},
                 "geometry": {"rings": [[[-74.601, -11.201], [-74.601, -11.199], [-74.599, -11.199],
                  [-74.599, -11.201], [-74.601, -11.201]]]}}
                """;
        JsonNode feature = new ObjectMapper().readTree(json);

        CatastroClient.PredioCatastral p = CatastroClient.leerPredio(feature);

        assertEquals("706980", p.codigo());
        assertEquals("120602", p.ubigeo());
        assertEquals(1.9793, p.areaHa());
        assertEquals(-11.2, p.latitud(), 1e-6);
        assertEquals(-74.6, p.longitud(), 1e-6);
    }

    @Test
    void buscar_codigoEncontrado_vinculaElDistritoPorUbigeo() {
        when(departamentoRepository.findById(12L)).thenReturn(Optional.of(junin()));
        when(catastroClient.buscarPorCodigo("JUNIN", "706980")).thenReturn(Optional.of(
                new CatastroClient.PredioCatastral("706980", "VILLA VICTORIA I", "JUNIN", "SATIPO", "COVIRIALI",
                        "120602", -11.2, -74.6, 1.9793)));
        Distrito coviriali = new Distrito();
        coviriali.setId(1150L);
        when(distritoRepository.findByUbigeo("120602")).thenReturn(Optional.of(coviriali));

        CatastroDTO r = catastroService.buscar(" 706980 ", 12L);

        assertEquals(1150L, r.distritoId());
        assertEquals(1.9793, r.areaHa());
    }

    @Test
    void buscar_codigoInexistente_lanza404ConOpcionDeMapa() {
        when(departamentoRepository.findById(12L)).thenReturn(Optional.of(junin()));
        when(catastroClient.buscarPorCodigo("JUNIN", "999999")).thenReturn(Optional.empty());

        RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class,
                () -> catastroService.buscar("999999", 12L));
        assertTrue(ex.getMessage().contains("mapa"));
    }

    @Test
    void buscar_servicioCaido_propagaIntegracionException() {
        when(departamentoRepository.findById(12L)).thenReturn(Optional.of(junin()));
        when(catastroClient.buscarPorCodigo(anyString(), anyString()))
                .thenThrow(new IntegracionException("El servicio MIDAGRI no está disponible temporalmente"));

        assertThrows(IntegracionException.class, () -> catastroService.buscar("706980", 12L));
    }

    @Test
    void buscar_codigoConCaracteresNoPermitidos_lanzaReglaNegocioSinConsultar() {
        assertThrows(ReglaNegocioException.class, () -> catastroService.buscar("1' OR '1'='1", 12L));
        verifyNoInteractions(catastroClient);
    }

    private Departamento junin() {
        Departamento d = new Departamento();
        d.setId(12L);
        d.setNombre("JUNIN");
        return d;
    }
}
