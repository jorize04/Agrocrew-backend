package pe.edu.upc.agrocrew.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agrocrew.dto.CalificacionDTO;
import pe.edu.upc.agrocrew.dto.CalificacionRequestDTO;
import pe.edu.upc.agrocrew.exceptions.RecursoNoEncontradoException;
import pe.edu.upc.agrocrew.models.Calificacion;
import pe.edu.upc.agrocrew.models.Evaluacion;
import pe.edu.upc.agrocrew.models.Usuario;
import pe.edu.upc.agrocrew.repositories.CalificacionRepository;
import pe.edu.upc.agrocrew.repositories.EvaluacionRepository;
import pe.edu.upc.agrocrew.services.CalificacionService;
import pe.edu.upc.agrocrew.services.UsuarioService;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalificacionServiceImpl implements CalificacionService {

    static final String MENSAJE_NUEVA = "¡Gracias por tu respuesta! Nos ayuda a mejorar AgroCrew.";
    static final String MENSAJE_ACTUALIZADA = "Actualizamos tu calificación. ¡Gracias por tu respuesta!";

    private final CalificacionRepository calificacionRepository;
    private final EvaluacionRepository evaluacionRepository;
    private final UsuarioService usuarioService;

    @Override
    @Transactional
    public CalificacionDTO calificar(Long evaluacionId, CalificacionRequestDTO dto) {
        Evaluacion evaluacion = evaluacionDelUsuarioActual(evaluacionId);

        Optional<Calificacion> existente = calificacionRepository.findByEvaluacionId(evaluacion.getId());
        boolean actualizada = existente.isPresent();
        Calificacion calificacion = existente.orElseGet(Calificacion::new);
        calificacion.setEvaluacion(evaluacion);
        calificacion.setPuntaje(dto.getPuntaje());
        calificacion.setComentario(limpiar(dto.getComentario()));

        Calificacion guardada = calificacionRepository.save(calificacion);
        log.info("Calificación {} evaluacion={} puntaje={}", actualizada ? "actualizada" : "registrada",
                evaluacionId, dto.getPuntaje());
        return convertir(guardada, actualizada, actualizada ? MENSAJE_ACTUALIZADA : MENSAJE_NUEVA);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CalificacionDTO> obtener(Long evaluacionId) {
        Evaluacion evaluacion = evaluacionDelUsuarioActual(evaluacionId);
        return calificacionRepository.findByEvaluacionId(evaluacion.getId())
                .map(c -> convertir(c, false, null));
    }

    /** Si la evaluación es de otro productor respondemos 404, igual que en predios. */
    private Evaluacion evaluacionDelUsuarioActual(Long evaluacionId) {
        Usuario usuario = usuarioService.obtenerUsuarioActual();
        return evaluacionRepository.findByIdAndPredioUsuarioId(evaluacionId, usuario.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Evaluación no encontrada"));
    }

    private static String limpiar(String comentario) {
        return comentario == null || comentario.isBlank() ? null : comentario.trim();
    }

    private static CalificacionDTO convertir(Calificacion c, boolean actualizada, String mensaje) {
        return new CalificacionDTO(c.getEvaluacion().getId(), c.getPuntaje(), c.getComentario(), c.getFecha(),
                c.getFechaActualizacion(), actualizada, mensaje);
    }
}
