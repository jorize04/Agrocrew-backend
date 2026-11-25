package pe.edu.upc.agrocrew.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upc.agrocrew.dto.DatoEconomicoRequestDTO;
import pe.edu.upc.agrocrew.dto.RentabilidadDTO;
import pe.edu.upc.agrocrew.exceptions.ConflictoException;
import pe.edu.upc.agrocrew.models.*;
import pe.edu.upc.agrocrew.repositories.CultivoRepository;
import pe.edu.upc.agrocrew.repositories.DatoEconomicoRepository;
import pe.edu.upc.agrocrew.repositories.DepartamentoRepository;
import pe.edu.upc.agrocrew.services.impl.DatoEconomicoServiceImpl;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatoEconomicoServiceImplTest {

    @Mock private DatoEconomicoRepository datoEconomicoRepository;
    @Mock private CultivoRepository cultivoRepository;
    @Mock private DepartamentoRepository departamentoRepository;
    @Mock private PredioService predioService;

    @InjectMocks
    private DatoEconomicoServiceImpl datoEconomicoService;

    private Cultivo papa;
    private Departamento junin;

    @BeforeEach
    void setUp() {
        papa = new Cultivo();
        papa.setId(1L);
        papa.setNombre("Papa");
        junin = new Departamento();
        junin.setId(12L);
        junin.setNombre("JUNIN");
    }

    @Test
    void registrar_mismoCultivoRegionYPeriodo_lanzaConflicto() {
        when(cultivoRepository.findById(1L)).thenReturn(Optional.of(papa));
        when(departamentoRepository.findById(12L)).thenReturn(Optional.of(junin));
        when(datoEconomicoRepository.existsByCultivoIdAndDepartamentoIdAndPeriodo(1L, 12L, "2025")).thenReturn(true);

        ConflictoException ex = assertThrows(ConflictoException.class, () -> datoEconomicoService.registrar(request()));
        assertTrue(ex.getMessage().contains("Actualice el registro existente"));
        verify(datoEconomicoRepository, never()).save(any());
    }

    @Test
    void rentabilidad_conDatos_calculaIngresoCostoYMargenPorHectarea() {
        when(predioService.obtenerPredioDelUsuarioActual(5L)).thenReturn(predioEnJunin());
        when(cultivoRepository.findById(1L)).thenReturn(Optional.of(papa));
        DatoEconomico dato = new DatoEconomico();
        dato.setPrecioChacraKg(new BigDecimal("1.20"));
        dato.setRendimientoKgHa(new BigDecimal("15000"));
        dato.setCostoProduccionHa(new BigDecimal("9500"));
        dato.setFuente("MIDAGRI - SIEA");
        dato.setPeriodo("2025");
        when(datoEconomicoRepository.findFirstByCultivoIdAndDepartamentoIdOrderByPeriodoDesc(1L, 12L))
                .thenReturn(Optional.of(dato));

        RentabilidadDTO r = datoEconomicoService.rentabilidad(5L, 1L);

        assertTrue(r.disponible());
        assertEquals(new BigDecimal("18000.00"), r.ingresoHa());
        assertEquals(new BigDecimal("9500.00"), r.costoHa());
        assertEquals(new BigDecimal("8500.00"), r.margenHa());
        assertEquals("2025", r.periodo());
        assertEquals("JUNIN", r.region());
    }

    @Test
    void rentabilidad_sinDatos_noMuestraCifras() {
        when(predioService.obtenerPredioDelUsuarioActual(5L)).thenReturn(predioEnJunin());
        when(cultivoRepository.findById(1L)).thenReturn(Optional.of(papa));
        when(datoEconomicoRepository.findFirstByCultivoIdAndDepartamentoIdOrderByPeriodoDesc(1L, 12L))
                .thenReturn(Optional.empty());

        RentabilidadDTO r = datoEconomicoService.rentabilidad(5L, 1L);

        assertFalse(r.disponible());
        assertNull(r.ingresoHa());
        assertNull(r.margenHa());
        assertTrue(r.mensaje().contains("no hay información económica suficiente"));
    }

    private Predio predioEnJunin() {
        Provincia huancayo = new Provincia();
        huancayo.setDepartamento(junin);
        Distrito elTambo = new Distrito();
        elTambo.setProvincia(huancayo);
        Predio predio = new Predio();
        predio.setId(5L);
        predio.setDistrito(elTambo);
        return predio;
    }

    private DatoEconomicoRequestDTO request() {
        DatoEconomicoRequestDTO dto = new DatoEconomicoRequestDTO();
        dto.setCultivoId(1L);
        dto.setDepartamentoId(12L);
        dto.setPrecioChacraKg(new BigDecimal("1.20"));
        dto.setRendimientoKgHa(new BigDecimal("15000"));
        dto.setCostoProduccionHa(new BigDecimal("9500"));
        dto.setFuente("MIDAGRI - SIEA");
        dto.setPeriodo("2025");
        return dto;
    }
}
