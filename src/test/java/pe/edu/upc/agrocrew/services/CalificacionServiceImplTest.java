package pe.edu.upc.agrocrew.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upc.agrocrew.dto.CalificacionDTO;
import pe.edu.upc.agrocrew.dto.CalificacionRequestDTO;
import pe.edu.upc.agrocrew.exceptions.RecursoNoEncontradoException;
import pe.edu.upc.agrocrew.models.Calificacion;
import pe.edu.upc.agrocrew.models.Evaluacion;
import pe.edu.upc.agrocrew.models.Usuario;
import pe.edu.upc.agrocrew.repositories.CalificacionRepository;
import pe.edu.upc.agrocrew.repositories.EvaluacionRepository;
import pe.edu.upc.agrocrew.services.impl.CalificacionServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalificacionServiceImplTest {

    @Mock private CalificacionRepository calificacionRepository;
    @Mock private EvaluacionRepository evaluacionRepository;
    @Mock private UsuarioService usuarioService;

    @InjectMocks
    private CalificacionServiceImpl calificacionService;

    private Evaluacion evaluacion;
    private CalificacionRequestDTO dto;

    @BeforeEach
    void setUp() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        when(usuarioService.obtenerUsuarioActual()).thenReturn(usuario);

        evaluacion = new Evaluacion();
        evaluacion.setId(7L);

        dto = new CalificacionRequestDTO();
        dto.setPuntaje(5);
        dto.setComentario("  Muy útil  ");
    }

    @Test
    void calificar_primeraVez_registraYAgradece() {
        when(evaluacionRepository.findByIdAndPredioUsuarioId(7L, 1L)).thenReturn(Optional.of(evaluacion));
        when(calificacionRepository.findByEvaluacionId(7L)).thenReturn(Optional.empty());
        when(calificacionRepository.save(any(Calificacion.class))).thenAnswer(inv -> inv.getArgument(0));

        CalificacionDTO r = calificacionService.calificar(7L, dto);

        assertFalse(r.actualizada());
        assertEquals(5, r.puntaje());
        assertEquals("Muy útil", r.comentario());
        assertTrue(r.mensaje().startsWith("¡Gracias"));
    }

    @Test
    void calificar_porSegundaVez_actualizaLaAnteriorSinCrearOtra() {
        Calificacion anterior = new Calificacion();
        anterior.setId(3L);
        anterior.setEvaluacion(evaluacion);
        anterior.setPuntaje(2);
        when(evaluacionRepository.findByIdAndPredioUsuarioId(7L, 1L)).thenReturn(Optional.of(evaluacion));
        when(calificacionRepository.findByEvaluacionId(7L)).thenReturn(Optional.of(anterior));
        when(calificacionRepository.save(any(Calificacion.class))).thenAnswer(inv -> inv.getArgument(0));

        CalificacionDTO r = calificacionService.calificar(7L, dto);

        assertTrue(r.actualizada());
        assertEquals(5, anterior.getPuntaje());
        verify(calificacionRepository).save(anterior);
    }

    @Test
    void calificar_evaluacionDeOtroUsuario_lanza404() {
        when(evaluacionRepository.findByIdAndPredioUsuarioId(7L, 1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> calificacionService.calificar(7L, dto));
        verify(calificacionRepository, never()).save(any());
    }
}
