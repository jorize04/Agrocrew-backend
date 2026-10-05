package pe.edu.upc.agrocrew.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upc.agrocrew.dto.PredioRequestDTO;
import pe.edu.upc.agrocrew.dto.PredioResponseDTO;
import pe.edu.upc.agrocrew.exceptions.RecursoNoEncontradoException;
import pe.edu.upc.agrocrew.exceptions.ReglaNegocioException;
import pe.edu.upc.agrocrew.models.*;
import pe.edu.upc.agrocrew.repositories.DistritoRepository;
import pe.edu.upc.agrocrew.repositories.PredioRepository;
import pe.edu.upc.agrocrew.services.impl.PredioServiceImpl;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PredioServiceImplTest {

    @Mock private PredioRepository predioRepository;
    @Mock private DistritoRepository distritoRepository;
    @Mock private UsuarioService usuarioService;
    @Mock private AlertaService alertaService;

    @InjectMocks
    private PredioServiceImpl predioService;

    private Usuario usuario;
    private Distrito distrito;
    private PredioRequestDTO dto;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);

        Departamento dep = new Departamento();
        dep.setNombre("JUNIN");
        Provincia prov = new Provincia();
        prov.setNombre("HUANCAYO");
        prov.setDepartamento(dep);
        distrito = new Distrito();
        distrito.setId(10L);
        distrito.setNombre("EL TAMBO");
        distrito.setProvincia(prov);
        distrito.setAltitudMsnm(3250);

        dto = new PredioRequestDTO();
        dto.setNombre(" Chacra La Esperanza ");
        dto.setDistritoId(10L);
        dto.setLatitud(-12.06);
        dto.setLongitud(-75.21);
        dto.setAreaHa(new BigDecimal("1.50"));
        dto.setFuenteAgua(FuenteAgua.SECANO);

        when(usuarioService.obtenerUsuarioActual()).thenReturn(usuario);
    }
/*Pruebas*
    @Test
    void registrar_sinAltitud_usaLaAltitudDelDistrito() {
        when(distritoRepository.findById(10L)).thenReturn(Optional.of(distrito));
        when(predioRepository.save(any(Predio.class))).thenAnswer(inv -> inv.getArgument(0));

        PredioResponseDTO resultado = predioService.registrar(dto);

        assertEquals(Integer.valueOf(3250), resultado.getAltitudMsnm());
        assertEquals("Chacra La Esperanza", resultado.getNombre());
        assertEquals("JUNIN", resultado.getDepartamento());
    }

    @Test
    void registrar_conDistritoInexistente_lanzaReglaNegocio() {
        when(distritoRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ReglaNegocioException.class, () -> predioService.registrar(dto));
        verify(predioRepository, never()).save(any());
    }

    @Test
    void obtener_predioDeOtroUsuario_lanzaNoEncontrado() {
        when(predioRepository.findByIdAndUsuarioIdAndActivoTrue(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> predioService.obtener(99L));
    }

    @Test
    void eliminar_marcaElPredioComoInactivo() {
        Predio predio = new Predio();
        predio.setActivo(true);
        when(predioRepository.findByIdAndUsuarioIdAndActivoTrue(5L, 1L)).thenReturn(Optional.of(predio));

        predioService.eliminar(5L);

        assertFalse(predio.getActivo());
        verify(predioRepository).save(predio);
    }
}
