package pe.edu.upc.agrocrew.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upc.agrocrew.dto.IndicadoresDTO;
import pe.edu.upc.agrocrew.dto.ReporteMensualDTO;
import pe.edu.upc.agrocrew.dto.ReporteNivelRiesgoDTO;
import pe.edu.upc.agrocrew.exceptions.ReglaNegocioException;
import pe.edu.upc.agrocrew.models.EstadoEvaluacion;
import pe.edu.upc.agrocrew.models.Rol;
import pe.edu.upc.agrocrew.repositories.*;
import pe.edu.upc.agrocrew.services.impl.ReporteServiceImpl;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceImplTest {

    @Mock private PredioRepository predioRepository;
    @Mock private EvaluacionRepository evaluacionRepository;
    @Mock private RecomendacionRepository recomendacionRepository;
    @Mock private PuntoCriticoRepository puntoCriticoRepository;
    @Mock private AlertaRepository alertaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private CultivoRepository cultivoRepository;

    @InjectMocks
    private ReporteServiceImpl reporteService;

    @Test
    void indicadores_calculaLosPorcentajesDeValidacion() {
        // Given: 8 evaluaciones, 6 completadas y 2 hechas sin alerta activa
        when(evaluacionRepository.count()).thenReturn(8L);
        when(evaluacionRepository.countByEstado(EstadoEvaluacion.COMPLETADA)).thenReturn(6L);
        when(evaluacionRepository.countByAlertaActivaFalse()).thenReturn(2L);
        when(usuarioRepository.countByRolNombre(Rol.PRODUCTOR)).thenReturn(5L);

        // When
        IndicadoresDTO r = reporteService.indicadores();

        // Then
        assertThat(r.porcentajeEvaluacionesCompletadas()).isEqualTo(75.0);
        assertThat(r.porcentajeEvaluacionesPreventivas()).isEqualTo(25.0);
        assertThat(r.productores()).isEqualTo(5L);
    }

    @Test
    void indicadores_sinEvaluaciones_noDivideEntreCero() {
        when(evaluacionRepository.count()).thenReturn(0L);

        IndicadoresDTO r = reporteService.indicadores();

        assertThat(r.porcentajeEvaluacionesCompletadas()).isZero();
    }

    @Test
    void puntosCriticosPorDepartamento_convierteLasFilasDelSqlNativo() {
        List<Object[]> filas = List.<Object[]>of(new Object[]{"Junín", "ALTO", 12L});
        when(puntoCriticoRepository.reportePuntosCriticosPorDepartamento()).thenReturn(filas);

        List<ReporteNivelRiesgoDTO> r = reporteService.puntosCriticosPorDepartamento();

        assertThat(r).containsExactly(new ReporteNivelRiesgoDTO("Junín", "ALTO", 12L));
    }

    @Test
    void alertasPorMes_convierteMesYCantidad() {
        List<Object[]> filas = List.<Object[]>of(new Object[]{3, "LLUVIA_INTENSA", 4L});
        when(alertaRepository.reporteAlertasPorMes(2026)).thenReturn(filas);

        List<ReporteMensualDTO> r = reporteService.alertasPorMes(2026);

        assertThat(r).containsExactly(new ReporteMensualDTO(3, "LLUVIA_INTENSA", 4L));
    }

    @Test
    void cultivosMasRecomendados_conFechasInvertidas_lanzaReglaNegocio() {
        assertThrows(ReglaNegocioException.class, () -> reporteService.cultivosMasRecomendados(
                null, LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1)));
        verifyNoInteractions(recomendacionRepository);
    }
}
