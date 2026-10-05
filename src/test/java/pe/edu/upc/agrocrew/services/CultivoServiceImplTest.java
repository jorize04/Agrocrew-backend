package pe.edu.upc.agrocrew.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upc.agrocrew.dto.CultivoRequestDTO;
import pe.edu.upc.agrocrew.dto.CultivoResponseDTO;
import pe.edu.upc.agrocrew.dto.RequerimientoCultivoDTO;
import pe.edu.upc.agrocrew.exceptions.ConflictoException;
import pe.edu.upc.agrocrew.exceptions.ReglaNegocioException;
import pe.edu.upc.agrocrew.models.*;
import pe.edu.upc.agrocrew.repositories.CultivoRepository;
import pe.edu.upc.agrocrew.repositories.GrupoCumRepository;
import pe.edu.upc.agrocrew.services.impl.CultivoServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CultivoServiceImplTest {

    @Mock private CultivoRepository cultivoRepository;
    @Mock private GrupoCumRepository grupoCumRepository;

    @InjectMocks
    private CultivoServiceImpl cultivoService;

    private CultivoRequestDTO dto;
##BeforeEach
    @BeforeEach
    void setUp() {
        RequerimientoCultivoDTO r = new RequerimientoCultivoDTO();
        r.setAltitudMin(2500);
        r.setAltitudMax(4200);
        r.setTemperaturaMin(8.0);
        r.setTemperaturaMax(20.0);
        r.setPrecipitacionMinMm(500.0);
        r.setPrecipitacionMaxMm(1200.0);
        r.setToleranciaInundacion(ToleranciaInundacion.BAJA);
        r.setRequiereRiego(false);

        dto = new CultivoRequestDTO();
        dto.setNombre("Papa");
        dto.setTipo(TipoCultivo.TRANSITORIO);
        dto.setGruposCum(List.of("a"));
        dto.setRequerimiento(r);
    }

    @Test
    void registrar_datosValidos_guardaCultivoConRequerimientoYGrupo() {
        GrupoCum grupoA = new GrupoCum();
        grupoA.setCodigo("A");
        when(cultivoRepository.existsByNombreIgnoreCase("Papa")).thenReturn(false);
        when(grupoCumRepository.findByCodigoIn(anyCollection())).thenReturn(List.of(grupoA));
        when(cultivoRepository.save(any(Cultivo.class))).thenAnswer(inv -> inv.getArgument(0));

        CultivoResponseDTO resultado = cultivoService.registrar(dto);

        assertEquals(List.of("A"), resultado.getGruposCum());
        assertEquals(Integer.valueOf(2500), resultado.getRequerimiento().getAltitudMin());
    }

    @Test
    void registrar_nombreRepetido_lanzaConflicto() {
        when(cultivoRepository.existsByNombreIgnoreCase("Papa")).thenReturn(true);

        assertThrows(ConflictoException.class, () -> cultivoService.registrar(dto));
        verify(cultivoRepository, never()).save(any());
    }

    @Test
    void registrar_altitudMinimaMayorQueMaxima_lanzaReglaNegocio() {
        dto.getRequerimiento().setAltitudMin(5000);
        when(cultivoRepository.existsByNombreIgnoreCase("Papa")).thenReturn(false);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> cultivoService.registrar(dto));
        assertEquals("La altitud mínima no puede ser mayor que la máxima", ex.getMessage());
    }

    @Test
    void registrar_grupoCumInexistente_lanzaReglaNegocio() {
        dto.setGruposCum(List.of("Z"));
        when(cultivoRepository.existsByNombreIgnoreCase("Papa")).thenReturn(false);
        when(grupoCumRepository.findByCodigoIn(anyCollection())).thenReturn(List.of());

        assertThrows(ReglaNegocioException.class, () -> cultivoService.registrar(dto));
    }
}
